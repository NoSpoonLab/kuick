package kuick.api

import com.google.gson.*
import io.ktor.http.Parameters
import kuick.json.Json
import kuick.json.Json.gson
import kuick.json.Json.jsonParser
import kuick.orm.clazz
import kotlin.reflect.KFunction


class IllegalRequestBody : Exception()

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
    val parameters = handler.parameters.drop(1)
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
                    extraArgs[parameter.name] != null -> {
                        extraArgs[parameter.name]
                    }

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
                            extraArgs[parameter.name]
                                    ?: throw IllegalArgumentException("Missing expected field $type", t)
                        }
                    }
                }
            }
}

fun String.toJsonArray() = (jsonParser.parse(this) as JsonArray)
fun JsonArray.asStringList() = this.map { it.asString }
fun Parameters.getAsSet(name: String) = this[name]?.toJsonArray()?.asStringList()?.toSet() ?: emptySet()

data class Node<T>(
        val value: T?,
        val children: List<Node<T>>
) {
    companion object
}


fun Node.Companion.emptyNode(): Node<String> = Node("", emptyList())

fun <T> emptyTree() = Node<T>(null, emptyList())

fun Parameters.getAsTree(name: String): Node<String> = this[name]?.toJsonArray()?.asStringList()
        ?.toTree()
        ?: emptyTree()

fun List<String>.toTree(rootValue: String? = null, distinct: Boolean = false): Node<String> = Node(
        value = rootValue,
        children = (if (distinct) distinct() else this).toNodeList()
)

private fun List<String>.toNodeList(): List<Node<String>> =
        groupBy { it.substringBefore(".") }
                .map {
                    val key = it.key // a
                    val values = it.value // a, a.a, a.b

                    val equealToKey = values.groupBy { it == key }
                    val children = (equealToKey[true] ?: emptyList()).map { Node.emptyNode() } +
                            (equealToKey[false] ?: emptyList()).map { it.substringAfter(".") }.toNodeList()

                    Node(value = key, children = children)
                }


inline fun <T> Iterable<T>.splitBy(keySelector: (T) -> Boolean): Pair<List<T>, List<T>> {
    val grouped = groupBy(keySelector)
    return Pair((grouped[true] ?: emptyList()), grouped[false] ?: emptyList())
}

suspend fun JsonElement.applyToEachObject(handler: suspend (JsonObject) -> Unit) {
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

fun List<Any>.toJson() = Json.toJson(this)
fun Map<String, Any>.toJson() = Json.toJson(this)
