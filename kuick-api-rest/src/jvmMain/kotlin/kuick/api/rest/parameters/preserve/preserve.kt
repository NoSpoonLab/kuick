package kuick.api.rest.parameters.preserve

import com.google.gson.JsonElement
import kuick.api.Node
import kuick.api.applyToEachObject
import kuick.api.emptyNode
import kuick.api.splitBy

/* Representation of fields parameter. Fields parameter should be defined according to following rules:
 * 1) Should contain only field names that exist in requested model
 * 2) If contains a nested field of related resource, related resource should also be preserved
 */
class FieldsParam constructor(
        val root: Node<String>
) {
    companion object {
        fun <T : Any?> create(root: Node<String>, relatedClass: Class<T>): FieldsParam {
            validateFieldsParamNode(root, relatedClass)
            return FieldsParam(root)
        }
    }
}

suspend fun JsonElement.preserveFields(fieldsParam: FieldsParam) = preserveFields(fieldsParam.root)

private suspend fun JsonElement.preserveFields(fieldsParam: Node<String>) {
    val nodesToPreserve = fieldsParam.children.filter { it != Node.emptyNode() }
    if (nodesToPreserve.isEmpty()) {
        return
    }

    val fieldsToPreserve = nodesToPreserve.map { it.value }.toSet()

    applyToEachObject { jsonObject ->
        jsonObject.entrySet().removeIf { it.key !in fieldsToPreserve }
        nodesToPreserve.forEach {
            if (jsonObject.has(it.value))
                jsonObject[it.value].preserveFields(it)
        }
    }
}

private fun <T> validateFieldsParamNode(node: Node<String>, relatedClass: Class<T>) {
    val result = mutableListOf<InvalidFieldParamException>()
    val nodeFieldNames = node.children
            .mapNotNull { it.value }
    val relatedClassFieldNames = relatedClass.declaredFields
            .map { it.name }
            .toSet()

    val notMatchingFields = nodeFieldNames
            .filter { !relatedClassFieldNames.contains(it) }
    if (notMatchingFields.isNotEmpty()) {
        result.add(InvalidFieldParamException.NotExistingFields(notMatchingFields))
    }

    val (nodesToInlcude, nodesWithoutProperDefinition) = node.children
            .splitBy { it.children.contains(Node.emptyNode()) }
            .let {
                Pair(it.first, it.second.filter { it != Node.emptyNode() })
            }

    if (nodesWithoutProperDefinition.isNotEmpty()) { // TODO provide option to ignore this case
        result.add(InvalidFieldParamException.OrphanFields(nodesWithoutProperDefinition.mapNotNull { it.value }))
    }

    if (result.isNotEmpty()) {
        throw InvalidFieldParamException.Composite(result)
    }
}
