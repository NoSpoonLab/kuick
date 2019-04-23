package kuick.api.rest


import com.google.gson.JsonArray
import com.google.gson.JsonElement
import com.google.gson.JsonObject
import com.google.gson.JsonParser
import com.google.inject.Injector
import io.ktor.application.call
import io.ktor.http.ContentType
import io.ktor.http.HttpMethod
import io.ktor.request.receiveText
import io.ktor.response.respondText
import io.ktor.routing.Route
import io.ktor.routing.route
import io.ktor.util.AttributeKey
import kuick.api.buildArgsFromObject
import kuick.api.invokeHandler
import kuick.json.Json.gson
import kotlin.reflect.KFunction
import kotlin.reflect.KProperty


data class RestRouting(
        val parent: Route,
        val resourceName: String,
        val api: Any,
        val injector: Injector
) {

    fun <T> registerRoute(route: RestRoute<T>): Route =
            parent.route(resourceName, method = route.httpMethod) {
                println("REST: ${route.httpMethod.value} /$resourceName -> ${route.handler}") // logging

                handle {
                    val jsonParser = JsonParser()

                    val args = buildArgsFromObject(
                            route.handler,
                            jsonParser.parse(call.receiveText()),
                            call.attributes.allKeys.map { it.name to call.attributes[it as AttributeKey<Any>] }.toMap()
                    )

                    val result = invokeHandler(api, route.handler, args)

                    val jsonResult = gson.toJsonTree(result)

                    //TODO is it a right place to handle these parameters?
                    val queryParameters = call.request.queryParameters
                    if (route.withFieldsParameter && queryParameters.contains("\$fields")) {
                        handleFieldsParam(jsonResult, queryParameters["\$fields"]!!)
                    }
                    if (route.withFieldsParameter && queryParameters.contains("\$include")) {
                        handleIncludeParam(jsonResult, queryParameters["\$include"]!!, route.includeParameterConfiguration)
                    }

                    call.respondText(jsonResult.toString(), ContentType.Application.Json) // serialization
                }
            }

    private fun handleFieldsParam(jsonResult: JsonElement, fieldsParam: String) {
        val fieldsParam = (JsonParser().parse(fieldsParam) as JsonArray).map { it.asString }.toHashSet()

        fun handleFields(jsonObject: JsonObject) {
            jsonObject.entrySet().toList().forEach {
                if (!fieldsParam.contains(it.key)) {
                    jsonObject.remove(it.key)
                }
            }
        }

        when {
            jsonResult.isJsonObject -> handleFields(jsonResult.asJsonObject)
            jsonResult.isJsonArray -> {
                jsonResult.asJsonArray.forEach { obj ->
                    handleFields(obj.asJsonObject)
                }
            }
            else -> {
            }
        }
    }

    private fun handleIncludeParam(jsonResult: JsonElement, includeParam: String, configuration: Map<String, (id: String) -> Any>) {
        val includeParam = (JsonParser().parse(includeParam) as JsonArray).map { it.asString }.toHashSet()

        fun handleInclude(jsonObject: JsonObject) {
            jsonObject.entrySet().toList().forEach {
                if (includeParam.contains(it.key)) {
                    val method = configuration.getValue(it.key)
                    jsonObject.remove(it.key)
                    jsonObject.add(
                            it.key,
                            gson.toJsonTree(
                                    method(it.value.asString)
                            )
                    )
                }
            }
        }

        when {
            jsonResult.isJsonObject -> handleInclude(jsonResult.asJsonObject)
            jsonResult.isJsonArray -> {
                jsonResult.asJsonArray.forEach { obj ->
                    handleInclude(obj.asJsonObject)
                }
            }
            else -> {
            }
        }
    }
}

class RestRoute<T>(
        val httpMethod: HttpMethod,
        val handler: KFunction<T>
) {
    // TODO create subclass "Configuration" (?)
    var withFieldsParameter = false
        private set

    var withIncludeParameter = false
        private set

    var includeParameterConfiguration: Map<String, (id: String) -> Any> = emptyMap()
        private set

    fun withFieldsParameter() {
        withFieldsParameter = true
    }

    fun withIncludeParameter(vararg configuration: Pair<KProperty<Any?>, (id: String) -> Any>) {
        withIncludeParameter = true
        includeParameterConfiguration = configuration.map { it.first.name to it.second }.toMap()
    }
}




