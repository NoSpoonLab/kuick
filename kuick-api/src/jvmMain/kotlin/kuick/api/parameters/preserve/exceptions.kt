package kuick.api.parameters.preserve

import kuick.api.parameters.InvalidParamException


open class InvalidFieldParamException(msg: String) : InvalidParamException("Invalid fields param definition. $msg") {
    class OrphanFields(orphanFieldNames: List<String>)
        : InvalidFieldParamException("Cannot include children of following fields $orphanFieldNames without including field itself")

    class NotExistingFields(notExistingFields: List<String>)
        : InvalidFieldParamException("Cannot include fields $notExistingFields because they don't exist in requested resource model")

    class Composite(exceptions: List<InvalidFieldParamException>)
        : InvalidFieldParamException(exceptions.map { it.message }.joinToString("; "))
}
