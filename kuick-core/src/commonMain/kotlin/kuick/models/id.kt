package kuick.models


interface Id { val id: String }

interface IdProvider {

    fun randomId(): String
}