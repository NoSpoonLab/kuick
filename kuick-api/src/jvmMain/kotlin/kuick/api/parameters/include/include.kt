package kuick.api.parameters.include

import com.google.gson.JsonElement
import kuick.api.Node
import kuick.api.applyToEachObject
import kuick.api.emptyNode
import kuick.api.splitBy
import kuick.json.Json.gson


/* Representation of include parameter. Include parameter should be defined according to following rules:
 * 1) Should contain only field names that exist in requested model
 * 2) If contains a nested field of related resource, related resource should also be included
 * 3) Should only contain fields for which include operation is supported (configured)
 */
data class IncludeParam private constructor(
        val root: Node<String>) {

    companion object {
        fun <T : Any?> create(root: Node<String>,
                              responseClass: Class<T>,
                              configuration: Map<String, suspend (id: String) -> Any>): IncludeParam {
            validateIncludeParamNode(root, responseClass, configuration)
            return IncludeParam(root)
        }
    }
}

suspend fun JsonElement.includeRelatedResources(includeParam: IncludeParam, configuration: Map<String, suspend (id: String) -> Any>) =
        includeRelatedResources(includeParam.root, configuration)

private suspend fun JsonElement.includeRelatedResources(includeParam: Node<String>, configuration: Map<String, suspend (id: String) -> Any>) {
    val fieldsToInclude = includeParam.children
            .mapNotNull { it.value }
            .toMutableSet()

    applyToEachObject { jsonObject ->
        jsonObject.entrySet().toList().forEach {
            if (it.key in fieldsToInclude) {
                configuration[it.key]?.let { method ->
                    jsonObject.remove(it.key)
                    jsonObject.add(
                            it.key,
                            gson.toJsonTree(
                                    method(it.value.asString)
                            )
                    )
                }
            }

            includeParam.children.forEach {
                if (jsonObject.has(it.value))
                    jsonObject[it.value].includeRelatedResources(it, configuration)
            }
        }
    }
}


private fun <T : Any?> validateIncludeParamNode(
        node: Node<String>,
        relatedClass: Class<T>,
        configuration: Map<String, suspend (id: String) -> Any>) {
    val result = mutableListOf<InvalidIncludeParamException>()

    val nodeFieldNames = node.children
            .mapNotNull { it.value }
    val relatedClassFieldNames = relatedClass.declaredFields
            .map { it.name }
            .toSet()

    val notMatchingFields = nodeFieldNames
            .filter { !relatedClassFieldNames.contains(it) }
    if (notMatchingFields.isNotEmpty()) {
        result.add(InvalidIncludeParamException.NotExistingFields(notMatchingFields))
    }

    val notConfiguredFields = nodeFieldNames.filter { !configuration.keys.contains(it) }
    if (notConfiguredFields.isNotEmpty()) {
        result.add(InvalidIncludeParamException.NotSupportedByApi(notConfiguredFields))
    }

    val (nodesToInlcude, nodesWithoutProperDefinition) = node.children
            .splitBy { it.children.contains(Node.emptyNode()) }
            .let {
                Pair(it.first, it.second.filter { it != Node.emptyNode() })
            }

    if (nodesWithoutProperDefinition.isNotEmpty()) { // TODO provide option to ignore this case
        result.add(InvalidIncludeParamException.OrphanFields(nodesWithoutProperDefinition.mapNotNull { it.value }))
    }

    if (result.isNotEmpty()) {
        throw InvalidIncludeParamException.Composite(result)
    }
}
