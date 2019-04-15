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
import kuick.samples.todo2.infrastructure.reflection.invokeWithParams
import kotlin.coroutines.Continuation
import kotlin.coroutines.suspendCoroutine
import kotlin.reflect.KFunction
import kotlin.reflect.KParameter
import kotlin.reflect.jvm.javaMethod


data class RestRouting(
        val kuickRouting: KuickRouting,
        val resourceName: String,
        val api: Any
) {
    var withFieldsParameter = false

    var withIncludeParameter = false
    var includeParameterConfiguration: Map<String, suspend (String) -> Any>? = null

    fun withFieldsParameter() {
        withFieldsParameter = true
    }

    fun withInlcudeParameter(configuration: Map<String, suspend (String) -> Any>) {
        withIncludeParameter = true
        includeParameterConfiguration = configuration
    }
}


fun <T> RestRouting.route(
        httpMethod: HttpMethod,
        handler: KFunction<T>,
        configuration: RestRouting.() -> Unit = {})
        : Route =
        kuickRouting.routing.route(resourceName, method = httpMethod) {
            configuration()
            handle {
                val result = invokeREST(call.receiveText(), handler, api) // serialization
                var jsonResult = gson.toJsonTree(result)

                val queryParameters = call.request.queryParameters
                val jsonParser = JsonParser()

                jsonResult = handleFieldsParam(queryParameters, jsonParser, jsonResult)


//        // Include
//
//        // for each property that supports include there has to be provided a resource getter in config
//        val configuration: Map<String, suspend (String) -> Any> =
//                mapOf(Todo::owner.name to userApi::getOne)
//
//        val includeParamJson = queryParameters["\$include"]
//        if (includeParamJson != null) {
//            val includeParam = (jsonParser.parse(includeParamJson) as JsonArray).map { it.asString }.toHashSet()
//            val res = JsonArray()
//            jsonResult.forEach { obj ->
//                val jsonObject = JsonObject()
//                obj.asJsonObject.entrySet().forEach {
//                    if (includeParam.contains(it.key)) {
//                        jsonObject.add(
//                                it.key,
//                                gson.toJsonTree(
//                                        configuration[it.key]!!(it.value.asString)
//                                )
//                        )
//                    } else {
//                        jsonObject.add(it.key, it.value)
//                    }
//                    res.add(jsonObject)
//                }
//                jsonResult = res
//            }
//        }

                call.respondText(jsonResult.toString(), ContentType.Application.Json) // serialization
            }
        }

private fun RestRouting.handleFieldsParam(queryParameters: Parameters, jsonParser: JsonParser, jsonResult: JsonElement): JsonElement {
    var jsonResult1 = jsonResult
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
                jsonResult1.isJsonObject ->
                    jsonResult1 = handleFields(jsonResult1.asJsonObject, fieldsParam)
                jsonResult1.isJsonArray -> {
                    val res = JsonArray()
                    jsonResult1.asJsonArray.forEach { obj ->
                        res.add(handleFields(obj.asJsonObject, fieldsParam))
                    }
                    jsonResult1 = res
                }
                else -> {
                }
            }
        }
    }
    return jsonResult1
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
    configuration.invoke(RestRouting(this, resourceName, api))

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
