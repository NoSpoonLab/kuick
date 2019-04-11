package kuick.samples.todo2

import com.google.gson.Gson
import com.google.gson.GsonBuilder
import com.google.inject.Injector
import io.ktor.application.Application
import io.ktor.application.call
import io.ktor.http.ContentType
import io.ktor.request.receiveText
import io.ktor.response.respondText
import io.ktor.routing.Route
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
    }
    installHttpExceptionsSupport()

    routing {
        rpcRouting<TodoApi>(injector)
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
            call.respondText(gson.toJson(result), ContentType.Application.Json) // serialization
        }
    }
}


