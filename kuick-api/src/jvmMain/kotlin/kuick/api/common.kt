package kuick.api

import com.google.gson.*
import kuick.json.Json.gson
import kuick.orm.clazz
import kuick.reflection.invokeWithParams
import kotlin.coroutines.suspendCoroutine
import kotlin.reflect.KFunction
import kotlin.reflect.jvm.javaMethod


class IllegalRequestBody : Exception()

suspend fun invokeHandler(api: Any, handler: KFunction<*>, args: Collection<Any?>): Any? =
        if (handler.isSuspend) {
            suspendCoroutine { c ->
                try {
                    val result = api.invokeWithParams(handler.javaMethod!!, args + c)
                    c.resumeWith(Result.success(result))
                } catch (t: Throwable) {
                    c.resumeWith(Result.failure(t))
                }
            }
        } else {
            api.invokeWithParams(handler.javaMethod!!, args)
        }

fun <T> buildArgsFromObject(handler: KFunction<T>,
                            requestParameters: JsonElement,
                            extraArgs: Map<String, Any>): Collection<Any?> {
    val requestParameters = when (requestParameters) {
        is JsonNull -> JsonObject()
        is JsonPrimitive -> if (requestParameters.asString.isEmpty()) JsonObject() else throw IllegalRequestBody()
        is JsonObject -> requestParameters
        else -> throw IllegalRequestBody()
    }
    return buildArgs(handler, requestParameters, extraArgs)
}

fun <T> buildArgsFromArray(handler: KFunction<T>,
                           requestParameters: JsonElement,
                           extraArgs: Map<String, Any>): Collection<Any?> {
    val requestParameters = when (requestParameters) {
        is JsonNull -> JsonArray()
        is JsonPrimitive -> if (requestParameters.asString.isEmpty()) JsonArray() else throw IllegalRequestBody()
        is JsonArray -> requestParameters
        else -> throw IllegalRequestBody()
    }
    return buildArgs(handler, requestParameters, extraArgs)
}


private fun <T> buildArgs(handler: KFunction<T>,
                          requestParameters: JsonElement,
                          extraArgs: Map<String, Any>): Collection<Any?> {
    val parameters = handler.parameters
            .subList(1, handler.parameters.size)
    return parameters
            .withIndex()
            .map { (i, parameter) ->

                fun getParameter(): JsonElement? = when {
                    requestParameters.isJsonArray -> requestParameters.asJsonArray[i]
                    requestParameters.isJsonObject -> requestParameters.asJsonObject[parameter.name]
                    else -> null
                }

                val type = parameter.type.clazz.java
                when {
                    // If there's an extra parameter, overload it to whatever is sent by client argument
                    extraArgs[parameter.name] != null -> { extraArgs[parameter.name] }

                    type.isAssignableFrom(String::class.java) -> {
                        val jsonParam = getParameter()
                        val value = when (jsonParam) {
                            is JsonNull, null -> null
                            else -> jsonParam.asString
                        }
                        value
                    }

                    else -> {
                        try {
                            val jsonParam = getParameter()
                            val value = when (jsonParam) {
                                is JsonNull, null -> null
                                else -> gson.fromJson(jsonParam.toString(), type)
                            }
                            value
                        } catch (t: Throwable) {
                            extraArgs[parameter.name] ?: throw IllegalArgumentException("Missing expected field $type", t)
                        }
                    }
                }
            }
}

