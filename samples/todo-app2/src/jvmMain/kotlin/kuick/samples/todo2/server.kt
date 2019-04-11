package kuick.samples.todo2

import com.google.gson.*
import com.google.inject.Injector
import io.ktor.application.Application
import io.ktor.application.call
import io.ktor.http.ContentType
import io.ktor.request.receiveText
import io.ktor.response.respondText
import io.ktor.routing.Route
import io.ktor.routing.get
import io.ktor.routing.post
import io.ktor.routing.routing
import io.ktor.server.engine.embeddedServer
import io.ktor.server.netty.Netty
import kuick.client.db.DbClientPool
import kuick.client.jdbc.JdbcDriver
import kuick.di.Guice
import kuick.di.bindPerCoroutineJob
import kuick.json.DateAdapter
import kuick.json.LocalDateTimeAdapter
import kuick.json.LocalTimeAdapter
import kuick.ktor.installContextPerRequest
import kuick.ktor.installHttpExceptionsSupport
import kuick.samples.todo2.infrastructure.invokeRPC
import kuick.samples.todo2.infrastructure.visitRPC
import java.time.LocalDateTime
import java.time.LocalTime
import java.util.*

suspend fun main(args: Array<String>) {
    embeddedServer(Netty, port = 8080) { module() }.start(wait = true)
}

fun Application.module() {
    val injector = Guice {
        bindPerCoroutineJob()
        configure()
    }

    // What's this? Database config also in Guice module
    installContextPerRequest(injector, DbClientPool { JdbcDriver.connectMemoryH2() }) {
        injector.getInstance(TodoRepository::class.java).init()
        injector.getInstance(UserRepository::class.java).init()
    }
    installHttpExceptionsSupport()

    routing {
        rpcRouting<TodoApi>(injector)

        restRouting(injector)
    }
}


// Infrastructure

val gson: Gson = GsonBuilder()
        //.registerTypeHierarchyAdapter(Id::class.java, IdGsonAdapter())
        //.registerTypeAdapter(CounterNumber::class.java, CounterNumberGsonAdapter())
        .registerTypeAdapter(Date::class.java, DateAdapter())
        .registerTypeAdapter(LocalDateTime::class.java, LocalDateTimeAdapter())
        .registerTypeAdapter(LocalTime::class.java, LocalTimeAdapter())
        .create()

inline fun <reified T> Route.rpcRouting(injector: Injector) {
    val api = injector.getInstance(T::class.java)!!
    api.visitRPC { srvName, method ->
        val path = "/rpc/$srvName/${method.name}"
        println("RPC: $path -> $method") // logging
        post(path) {
            //            val parameters = call.receiveParameters()
            val result = invokeRPC(call.receiveText(), method, api) // serialization
            // pipeline & context
            // Gson: ExclusionStrategy to implement `$fields`
            call.respondText(gson.toJson(result), ContentType.Application.Json) // serialization
        }
    }
}

fun Route.restRouting(
        injector: Injector
) {
    val todoApi = injector.getInstance(TodoApi::class.java)!!
    val userApi = injector.getInstance(UserApi::class.java)!!

    get("todos") {
        val result = todoApi.getAll()

        val jsonParser = JsonParser()

        val queryParameters = call.request.queryParameters

        var jsonResult = gson.toJsonTree(result).asJsonArray

        // Fields
        val fieldsParamJson = queryParameters["\$fields"]
        if (fieldsParamJson != null) {
            val fieldsParam = (jsonParser.parse(fieldsParamJson) as JsonArray).map { it.asString }.toHashSet()
            val res = JsonArray()
            jsonResult.forEach { obj ->
                val jsonObject = JsonObject()
                obj.asJsonObject.entrySet().forEach {
                    if (fieldsParam.contains(it.key)) jsonObject.add(it.key, it.value)
                }
                res.add(jsonObject)
            }
            jsonResult = res
        }

        // Include

        // for each property that supports include there has to be provided a resource getter in config
        val configuration: Map<String, suspend (String) -> Any> =
                mapOf(Todo::owner.name to userApi::getOne )

        val includeParamJson = queryParameters["\$include"]
        if (includeParamJson != null) {
            val includeParam = (jsonParser.parse(includeParamJson) as JsonArray).map { it.asString }.toHashSet()
            val res = JsonArray()
            jsonResult.forEach { obj ->
                val jsonObject = JsonObject()
                obj.asJsonObject.entrySet().forEach {
                    if (includeParam.contains(it.key)) {
                        jsonObject.add(
                                it.key,
                                gson.toJsonTree(
                                        configuration[it.key]!!(it.value.asString)
                                )
                        )
                    } else {
                        jsonObject.add(it.key, it.value)
                    }
                    res.add(jsonObject)
                }
                jsonResult = res
            }
        }

        call.respondText(jsonResult.toString(), ContentType.Application.Json) // serialization

    }

    post("todos") {
        data class Request(val text: String, val owner: String)


        val bodyJson = call.receiveText()

        val jsonParser = JsonParser()
        val fromJson = jsonParser.parse(bodyJson) as JsonObject

        val result = todoApi.add(fromJson["text"].asString, fromJson["owner"].asString)
        val jsonResult = gson.toJson(result)

        call.respondText(jsonResult, ContentType.Application.Json) // serialization
    }

    post("users") {
        data class Request(val name: String)

        val bodyJson = call.receiveText()

        val jsonParser = JsonParser()
        val fromJson = jsonParser.parse(bodyJson) as JsonObject

        val result = userApi.add(fromJson["name"].asString)
        val jsonResult = gson.toJson(result)

        call.respondText(jsonResult, ContentType.Application.Json) // serialization
    }

}

