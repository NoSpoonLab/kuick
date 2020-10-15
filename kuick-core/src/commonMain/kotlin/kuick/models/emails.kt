package kuick.models


data class Email(val email: String) {
    fun normalized() = email.toLowerCase().trim()

    fun validate() {
        if (!isValid()) error("Invalid email $this")
    }

    fun isValid(): Boolean {
        if (this.email.length > 128) return false
        return this.email.matches(EMAIL_VALIDATION_REGEX)
    }
}

// http://regexlib.com/Search.aspx?k=email&AspxAutoDetectCookieSupport=1
// https://tools.ietf.org/html/rfc2822
private val EMAIL_VALIDATION_REGEX = Regex("^([a-zA-Z0-9_\\-\\.]+)@((\\[[0-9]{1,3}\\.[0-9]{1,3}\\.[0-9]{1,3}\\.)|(([a-zA-Z0-9\\-]+\\.)+))([a-zA-Z]{2,4}|[0-9]{1,3})(\\]?)\$")
