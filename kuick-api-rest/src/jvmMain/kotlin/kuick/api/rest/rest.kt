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
import kuick.json.Json.gson
import kotlin.reflect.KFunction
import kotlin.reflect.KProperty
import kotlin.reflect.full.callSuspend


data class RestRouting(
        val parent: Route,
        val resourceName: String,
        val api: Any,
        val injector: Injector
) {

    fun <T> registerRoute(route: RestRoute<T>): Route =
            parent.route(resourceName, method = route.httpMethod) {
                println("REST: ${route.httpMethod.value} /$resourceName -> ${route.handler}") // logging
                val jsonParser = JsonParser()

                handle {

                    val args = buildArgsFromObject(
                            route.handler,
                            jsonParser.parse(call.receiveText()),
                            call.attributes.allKeys.map { it.name to call.attributes[it as AttributeKey<Any>] }.toMap()
                    )

                    val result = route.handler.callSuspend(api, *args.toTypedArray())

                    val jsonResult = gson.toJsonTree(result)

                    val queryParameters = call.request.queryParameters
                    if (route.config.withFieldsParameter && "\$fields" in queryParameters) {
                        handleFieldsParam(jsonResult, queryParameters["\$fields"]!!)
                    }
                    if (route.config.includeParameterConfiguration != null && "\$include" in queryParameters) {
                        handleIncludeParam(jsonResult, queryParameters["\$include"]!!, route.config.includeParameterConfiguration!!)
                    }

                    call.respondText(jsonResult.toString(), ContentType.Application.Json) // serialization
                }
            }

    private fun handleFieldsParam(jsonResult: JsonElement, fieldsParam: String) {
        val fieldsParam = (JsonParser().parse(fieldsParam) as JsonArray).map { it.asString }.toSet()
        jsonResult.applyToEachObject { jsonObject ->
            jsonObject.entrySet().removeIf { it.key !in fieldsParam }
        }
    }

    private fun handleIncludeParam(jsonResult: JsonElement, includeParam: String, configuration: Map<String, (id: String) -> Any>) {
        val includeParam = (JsonParser().parse(includeParam) as JsonArray).map { it.asString }.toSet()
        jsonResult.applyToEachObject { jsonObject ->
            jsonObject.entrySet().toList().forEach {
                if (it.key in includeParam) {
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
    }

    private fun JsonElement.applyToEachObject(handler: (JsonObject) -> Unit) {
        when {
            isJsonObject -> handler(asJsonObject)
            isJsonArray -> {
                asJsonArray.forEach {
                    handler(it.asJsonObject)
                }
            }
            else -> {
            }
        }
    }
}

class RestRoute<T>(
        val httpMethod: HttpMethod,
        val handler: KFunction<T>,
        val config: Configuration = Configuration()
) {

    class Configuration {
        var withFieldsParameter: Boolean = false
            private set
        var includeParameterConfiguration: Map<String, (id: String) -> Any>? = null
            private set

        fun withFieldsParameter() {
            withFieldsParameter = true
        }

        fun withIncludeParameter(vararg configuration: Pair<KProperty<Any?>, (id: String) -> Any>) {
            includeParameterConfiguration = configuration
                    .map { it.first.name to it.second }.toMap()
        }
    }

}


