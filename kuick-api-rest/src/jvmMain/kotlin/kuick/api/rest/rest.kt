package kuick.api.rest


import com.google.gson.JsonArray
import com.google.gson.JsonElement
import com.google.gson.JsonObject
import com.google.gson.JsonParser
import com.google.inject.Injector
import io.ktor.application.call
import io.ktor.http.ContentType
import io.ktor.http.HttpMethod
import io.ktor.http.Parameters
import io.ktor.request.receiveText
import io.ktor.response.respondText
import io.ktor.routing.Route
import io.ktor.routing.route
import kuick.api.buildArgs
import kuick.api.invokeHandler
import kuick.json.Json.gson
import kuick.ktor.KuickRouting
import kotlin.reflect.KFunction
import kotlin.reflect.KProperty
import kotlin.reflect.full.callSuspend


data class RestRouting(
        val kuickRouting: KuickRouting,
        val resourceName: String,
        val api: Any,
        val injector: Injector
) {

    fun <T> registerRoute(route: RestRoute<T>): Route =
            kuickRouting.routing.route(resourceName, method = route.httpMethod) {
                println("REST: ${route.httpMethod.value} /$resourceName -> ${route.handler}") // logging

                handle {
                    val args = buildArgs(
                            route.handler,
                            gson.toJsonTree(call.receiveText()),
                            emptyMap() // TODO Pipes
                    )

                    val result = invokeHandler(api, route.handler, args)

                    // TODO make it better
                    var jsonResult = gson.toJsonTree(result)
                    val queryParameters = call.request.queryParameters
                    val jsonParser = JsonParser()
                    jsonResult = handleFieldsParam(queryParameters, jsonParser, jsonResult, route)
                    jsonResult = handleIncludeParam(queryParameters, jsonParser, jsonResult, route)


                    call.respondText(jsonResult.toString(), ContentType.Application.Json) // serialization
                }
            }

    private fun handleFieldsParam(queryParameters: Parameters, jsonParser: JsonParser, jsonResult: JsonElement, route: RestRoute<*>): JsonElement {
        var newJsonResult = jsonResult
        if (route.withFieldsParameter) {
            val fieldsParamJson = queryParameters["\$fields"]
            if (fieldsParamJson != null) {
                val fieldsParam = (jsonParser.parse(fieldsParamJson) as JsonArray).map { it.asString }.toHashSet()

                fun handleFields(jsonObject: JsonObject, fieldsParam: Set<String>): JsonElement {
                    val newJsonObject = JsonObject()
                    jsonObject.asJsonObject.entrySet().forEach {
                        if (fieldsParam.contains(it.key)) newJsonObject.add(it.key, it.value)
                    }
                    return newJsonObject
                }

                when {
                    newJsonResult.isJsonObject ->
                        newJsonResult = handleFields(newJsonResult.asJsonObject, fieldsParam)
                    newJsonResult.isJsonArray -> {
                        val res = JsonArray()
                        newJsonResult.asJsonArray.forEach { obj ->
                            res.add(handleFields(obj.asJsonObject, fieldsParam))
                        }
                        newJsonResult = res
                    }
                    else -> {
                    }
                }
            }
        }
        return newJsonResult
    }

    private suspend fun handleIncludeParam(queryParameters: Parameters, jsonParser: JsonParser, jsonResult: JsonElement, route: RestRoute<*>): JsonElement {
        var newJsonResult = jsonResult
        if (route.withIncludeParameter) {
            val includeParamJson = queryParameters["\$include"]
            if (includeParamJson != null) {
                val includeParam = (jsonParser.parse(includeParamJson) as JsonArray).map { it.asString }.toHashSet()

                suspend fun handleInclude(jsonObject: JsonObject): JsonElement {
                    val newJsonObject = JsonObject()
                    jsonObject.entrySet().forEach {
                        if (includeParam.contains(it.key)) {
                            val method = route.includeParameterConfiguration.getValue(it.key)
//                        val owner = injector.getInstance(UserApi::class.java)
                            newJsonObject.add(
                                    it.key,
                                    gson.toJsonTree(
                                            method.callSuspend(it.value.asString)
                                    )
                            )
                        } else {
                            newJsonObject.add(it.key, it.value)
                        }
                    }
                    return newJsonObject
                }

                when {
                    newJsonResult.isJsonObject ->
                        newJsonResult = handleInclude(newJsonResult.asJsonObject)
                    newJsonResult.isJsonArray -> {
                        val res = JsonArray()
                        newJsonResult.asJsonArray.forEach { obj ->
                            res.add(handleInclude(obj.asJsonObject))
                        }
                        newJsonResult = res
                    }
                    else -> {
                    }
                }
            }
        }
        return newJsonResult
    }

}

class RestRoute<T>(
        val httpMethod: HttpMethod,
        val handler: KFunction<T>
) {
    var withFieldsParameter = false
        private set

    var withIncludeParameter = false
        private set

    var includeParameterConfiguration: Map<String, KFunction<Any>> = emptyMap()
        private set

    fun withFieldsParameter() {
        withFieldsParameter = true
    }

    fun withIncludeParameter(vararg configuration: Pair<KProperty<Any>, KFunction<Any>>) {
        withIncludeParameter = true
        includeParameterConfiguration = configuration.map { it.first.name to it.second }.toMap()
    }
}




