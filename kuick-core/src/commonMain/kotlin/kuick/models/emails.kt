package kuick.models


data class Email(val email: String) {
    fun normalized() = email.toLowerCase().trim()
}