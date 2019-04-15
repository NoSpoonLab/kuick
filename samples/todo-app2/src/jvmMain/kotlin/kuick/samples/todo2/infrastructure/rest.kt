package kuick.samples.todo2.infrastructure

import com.google.gson.*
import com.google.inject.Injector
import io.ktor.application.call
import io.ktor.http.ContentType
import io.ktor.http.HttpMethod
import io.ktor.http.Parameters
import io.ktor.request.receiveText
import io.ktor.response.respondText
import io.ktor.routing.Route
import io.ktor.routing.route
import kuick.orm.clazz
import kuick.samples.todo2.UserApi
import kuick.samples.todo2.infrastructure.reflection.invokeWithParams
import kotlin.coroutines.Continuation
import kotlin.coroutines.suspendCoroutine
import kotlin.reflect.KFunction
import kotlin.reflect.KParameter
import kotlin.reflect.KProperty
import kotlin.reflect.full.callSuspend
import kotlin.reflect.jvm.javaMethod


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
        : Route =
        kuickRouting.routing.route(resourceName, method = httpMethod) {
            println("REST: ${httpMethod.value} /$resourceName -> $handler") // logging
            configuration()
            handle {
                val result = invokeREST(call.receiveText(), handler, api) // serialization
                var jsonResult = gson.toJsonTree(result)

                val queryParameters = call.request.queryParameters
                val jsonParser = JsonParser()

                jsonResult = handleFieldsParam(queryParameters, jsonParser, jsonResult)
                jsonResult = handleIncludeParam(queryParameters, jsonParser, jsonResult)


//        // Include
//

                call.respondText(jsonResult.toString(), ContentType.Application.Json) // serialization
            }
        }

suspend private fun RestRouting.handleIncludeParam(queryParameters: Parameters, jsonParser: JsonParser, jsonResult: JsonElement): JsonElement {
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
                        val owner = injector.getInstance(UserApi::class.java)
                        newJsonObject.add(
                                it.key,
                                gson.toJsonTree(
                                        method.callSuspend(owner, it.value.asString)
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

fun <T> RestRouting.get(handler: KFunction<T>, configuration: RestRouting.() -> Unit = {}): Route = route(HttpMethod.Get, handler, configuration)
fun <T> RestRouting.put(handler: KFunction<T>, configuration: RestRouting.() -> Unit = {}): Route = route(HttpMethod.Put, handler, configuration)
fun <T> RestRouting.post(handler: KFunction<T>, configuration: RestRouting.() -> Unit = {}): Route = route(HttpMethod.Post, handler, configuration)
fun <T> RestRouting.delete(handler: KFunction<T>, configuration: RestRouting.() -> Unit = {}): Route = route(HttpMethod.Delete, handler, configuration)
fun <T> RestRouting.patch(handler: KFunction<T>, configuration: RestRouting.() -> Unit = {}): Route = route(HttpMethod.Patch, handler, configuration)

inline fun <reified T> KuickRouting.restRouting(
        injector: Injector,
        resourceName: String,
        configuration: RestRouting.() -> Unit
) {
    val api = injector.getInstance(T::class.java)!!
    configuration.invoke(RestRouting(this, resourceName, api, injector))

}

suspend fun <T> invokeREST(paramsJson: String, method: KFunction<T>, obj: Any, extraArgs: List<Any> = emptyList()): Any? {
    val res = JsonParser().parse(paramsJson)
    val jsonArray = when (res) {
        is JsonNull -> JsonObject()
        is JsonObject -> res as JsonObject
        else -> throw IllegalRPCBody()
    }

    return suspendCoroutine { c ->
        val args = buildArgs(method.parameters, jsonArray, c, extraArgs.associateBy { it::class.java })
        try {
            val result = obj.invokeWithParams(method.javaMethod!!, args)
            //println("2 - Result: $result")
            c.resumeWith(Result.success(result))
        } catch (t: Throwable) {
            c.resumeWith(Result.failure(t))
        }
    }
}

private fun buildArgs(parameters: List<KParameter>,
                      bodyValues: JsonObject,
                      c: Continuation<Any>,
                      extraArgs: Map<Class<out Any>, Any>): Collection<Any?> {
    return parameters.subList(1, parameters.size).withIndex().map { (i, parameter) ->
        val type = parameter.type.clazz.java
        //println("ARG[${type.simpleName}]")
        val name = parameter.name
        when {
            type.isAssignableFrom(Continuation::class.java) -> c
            type.isAssignableFrom(String::class.java) -> {
                val jsonParam = bodyValues[name]
                val value = when (jsonParam) {
                    is JsonNull, null -> null
                    else -> jsonParam.asString
                }
                value
            }
            // If there's an extra parameter, overload it to whatever is sent by client argument
            extraArgs.get(type) != null -> extraArgs.get(type)
            else -> {
                try {
                    val jsonParam = bodyValues[name]
                    val value = when (jsonParam) {
                        is JsonNull, null -> extraArgs.get(type)
                        else -> gson.fromJson(jsonParam.toString(), type)
                    }
                    value
                } catch (t: Throwable) {
                    extraArgs.get(type) ?: throw IllegalArgumentException("Missing expected field ${type}", t)
                }
            }
        }
    } + c
}
