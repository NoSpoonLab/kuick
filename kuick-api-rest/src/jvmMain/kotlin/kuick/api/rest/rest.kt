package kuick.api.rest


import com.google.gson.JsonElement
import com.google.gson.JsonObject
import com.google.inject.Injector
import io.ktor.application.call
import io.ktor.http.ContentType
import io.ktor.http.HttpMethod
import io.ktor.request.receiveText
import io.ktor.response.respondText
import io.ktor.routing.Route
import io.ktor.routing.route
import io.ktor.util.AttributeKey
import kuick.api.Node
import kuick.api.buildArgsFromObject
import kuick.api.emptyNode
import kuick.api.getAsTree
import kuick.json.Json.gson
import kuick.json.Json.jsonParser
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

                handle {

                    val args = buildArgsFromObject(
                            route.handler,
                            jsonParser.parse(call.receiveText()),
                            call.attributes.allKeys.map { it.name to call.attributes[it as AttributeKey<Any>] }.toMap()
                    )

                    val result = route.handler.callSuspend(api, *args.toTypedArray())

                    val jsonResult = gson.toJsonTree(result)

                    val queryParameters = call.request.queryParameters

                    //TODO handle case when we include smth just to cut it out when filtering (which would be stupid AF)
                    if (route.config.includeParameterConfiguration != null && "\$include" in queryParameters) {
                        val includeParam = queryParameters.getAsTree("\$include")
                        jsonResult.includeRelatedResources(includeParam, route.config.includeParameterConfiguration!!)
                    }

                    if (route.config.withFieldsParameter && "\$fields" in queryParameters) {
                        val fieldsParam = queryParameters.getAsTree("\$fields")
                        jsonResult.preserveFields(fieldsParam)
                    }

                    call.respondText(jsonResult.toString(), ContentType.Application.Json) // serialization
                }
            }

    inline fun <T> Iterable<T>.splitBy(keySelector: (T) -> Boolean): Pair<List<T>, List<T>> {
        val grouped = groupBy(keySelector)
        return Pair((grouped[true] ?: emptyList()), grouped[false] ?: emptyList())
    }

    private suspend fun JsonElement.preserveFields(fieldsParam: Node<String>) {
        val (nodesToPreserve, nodesWithoutProperDefinition) = fieldsParam.children
                .splitBy { it.children.contains(Node.emptyNode()) }
                .let {
                    Pair(it.first, it.second.filter { it != Node.emptyNode() })
                }

        if (nodesWithoutProperDefinition.isNotEmpty()) { // TODO provide option to ignore this case
            throw RuntimeException("Invalid fields param definition. Cannot preserve children of following fields " +
                    nodesWithoutProperDefinition.map { it.value } +
                    " without preserving field itself"
                    // TODO Exception class + handling
            )
        }

        if (nodesToPreserve.isEmpty()) {
            return
        }

        val fieldsToPreserve = nodesToPreserve.map { it.value }.toSet()

        applyToEachObject { jsonObject ->
            jsonObject.entrySet().removeIf { it.key !in fieldsToPreserve }
            nodesToPreserve.forEach {
                if (jsonObject.has(it.value))
                    jsonObject[it.value].preserveFields(it)
//                else
//                    throw RuntimeException("Invalid fields param definition. Field [${it.value}] doesn't exist in the response.")
            }
        }
    }

    private suspend fun JsonElement.includeRelatedResources(includeParam: Node<String>, configuration: Map<String, suspend (id: String) -> Any>) {
        val (nodesToInlcude, nodesWithoutProperDefinition) = includeParam.children
                .splitBy { it.children.contains(Node.emptyNode()) }
                .let {
                    Pair(it.first, it.second.filter { it != Node.emptyNode() })
                }

        if (nodesWithoutProperDefinition.isNotEmpty()) { // TODO provide option to ignore this case
            throw RuntimeException("Invalid include param definition. Cannot include children of following fields " +
                    nodesWithoutProperDefinition.map { it.value } +
                    " without including field itself"
                    // TODO Exception class + handling
            )
        }

        val fieldsToInclude = nodesToInlcude
                .mapNotNull { it.value }
                .toMutableSet()

        applyToEachObject { jsonObject ->
            jsonObject.entrySet().toList().forEach {
                if (it.key in fieldsToInclude) {
                    val method = configuration[it.key] // TODO provide option to ignore this case
                            ?: throw RuntimeException("Invalid include param definition." +
                                    " Cannot include field [${it.key}] because this operation is not supported for this field}"
                            ) // TODO Exception class + handling
                    jsonObject.remove(it.key)
                    jsonObject.add(
                            it.key,
                            gson.toJsonTree(
                                    method(it.value.asString)
                            )
                    )
                    fieldsToInclude.remove(it.key)
                }

                nodesToInlcude.forEach {
                    if (jsonObject.has(it.value))
                        jsonObject[it.value].includeRelatedResources(it, configuration)
//                else
//                    throw RuntimeException("Invalid fields param definition. Field [${it.value}] doesn't exist in the response.")
                }
            }

//            if (fieldsToInclude.isNotEmpty()) { // TODO provide option to ignore this case
//                throw RuntimeException("Invalid include param definition." +
//                        " Cannot include fields [$fieldsToInclude] because they don't exist in response."
//                ) // TODO Exception class + handling
//            }
        }
    }

    private suspend fun JsonElement.applyToEachObject(handler: suspend (JsonObject) -> Unit) {
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
        var includeParameterConfiguration: Map<String, suspend (id: String) -> Any>? = null
            private set

        fun withFieldsParameter() {
            withFieldsParameter = true
        }

        fun withIncludeParameter(vararg configuration: Pair<KProperty<Any?>, suspend (id: String) -> Any>) {
            includeParameterConfiguration = configuration
                    .map { it.first.name to it.second }.toMap()
        }
    }

}


