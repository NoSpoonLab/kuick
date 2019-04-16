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
    var withFieldsParameter = false

    var withIncludeParameter = false
    var includeParameterConfiguration: Map<String, KFunction<Any>> = emptyMap()

    fun withFieldsParameter() {
        withFieldsParameter = true
    }

    fun withIncludeParameter(vararg configuration: Pair<KProperty<Any>, KFunction<Any>>) {
        withIncludeParameter = true
        includeParameterConfiguration = configuration.map { it.first.name to it.second }.toMap()
    }
}


fun <T> RestRouting.route(
        httpMethod: HttpMethod,
        handler: KFunction<T>,
        configuration: RestRouting.() -> Unit = {})
        : Route {

    return kuickRouting.routing.route(resourceName, method = httpMethod) {
        println("REST: ${httpMethod.value} /$resourceName -> $handler") // logging
        configuration()

        handle {
            val args = buildArgs(
                    handler,
                    gson.toJsonTree(call.receiveText()),
                    emptyMap() // TODO Pipes
            )

            val result = invokeHandler(api, handler, args)

            // TODO make it better
            var jsonResult = gson.toJsonTree(result)
            val queryParameters = call.request.queryParameters
            val jsonParser = JsonParser()
            jsonResult = handleFieldsParam(queryParameters, jsonParser, jsonResult)
            jsonResult = handleIncludeParam(queryParameters, jsonParser, jsonResult)


            call.respondText(jsonResult.toString(), ContentType.Application.Json) // serialization
        }
    }
}

private suspend fun RestRouting.handleIncludeParam(queryParameters: Parameters, jsonParser: JsonParser, jsonResult: JsonElement): JsonElement {
    var newJsonResult = jsonResult
    if (withIncludeParameter) {
        val includeParamJson = queryParameters["\$include"]
        if (includeParamJson != null) {
            val includeParam = (jsonParser.parse(includeParamJson) as JsonArray).map { it.asString }.toHashSet()

            suspend fun handleInclude(jsonObject: JsonObject): JsonElement {
                val newJsonObject = JsonObject()
                jsonObject.entrySet().forEach {
                    if (includeParam.contains(it.key)) {
                        val method = includeParameterConfiguration.getValue(it.key)
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

private fun RestRouting.handleFieldsParam(queryParameters: Parameters, jsonParser: JsonParser, jsonResult: JsonElement): JsonElement {
    var newJsonResult = jsonResult
    if (withFieldsParameter) {
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


