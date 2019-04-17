package kuick.api

import com.google.gson.JsonElement
import com.google.gson.JsonNull
import kuick.json.Json.gson
import kuick.orm.clazz
import kuick.reflection.invokeWithParams
import kotlin.coroutines.suspendCoroutine
import kotlin.reflect.KFunction
import kotlin.reflect.jvm.javaMethod


class IllegalRPCBody : Exception()

fun <T> buildArgs(handler: KFunction<T>,
              requestParameters: JsonElement,
              extraArgs: Map<Class<out Any>, Any>): Collection<Any?> {

//    TODO Handle it
//    val jsonArray = when (res) {
//        is JsonNull -> JsonObject()
//        is JsonObject -> res as JsonObject
//        else -> throw IllegalRPCBody()

    val parameters = handler.parameters
    return parameters.subList(1, parameters.size)
            .withIndex()
            .map { (i, parameter) ->

                fun get(): JsonElement? {
                    return when {
                        requestParameters.isJsonArray -> requestParameters.asJsonArray[i]
                        requestParameters.isJsonObject -> requestParameters.asJsonObject[parameter.name]
                        else -> null
                    }
                }

                val type = parameter.type.clazz.java
                when {
                    type.isAssignableFrom(String::class.java) -> {
                        val jsonParam = get()
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
                            val jsonParam = get()
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
            }
}


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

