package kuick.api.parameters.include

import kuick.api.parameters.InvalidParamException


open class InvalidIncludeParamException(msg: String) : InvalidParamException("Invalid include param definition. $msg") {
    class OrphanFields(orphanFieldNames: List<String>)
        : InvalidIncludeParamException("Cannot include children of following fields $orphanFieldNames without including field itself")

    class NotSupportedByApi(notSupportedFields: List<String>)
        : InvalidIncludeParamException("Cannot include fields $notSupportedFields because this operation is not supported for this field")

    class NotExistingFields(notExistingFields: List<String>)
        : InvalidIncludeParamException("Cannot include fields $notExistingFields because they don't exist in requested resource model")

    class Composite(exceptions: List<InvalidIncludeParamException>) : InvalidIncludeParamException(exceptions.map { it.message }.joinToString("; "))
}
