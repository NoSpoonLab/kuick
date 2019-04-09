package kuick.http

/*
enum class HttpMethod {
    GET, POST, DELETE, PUT, ANY;
    companion object {
        val BY_NAME = values().associateBy { it.name.toLowerCase() }
        fun parse(name: String) = BY_NAME[name.toLowerCase()] ?: HttpMethod.ANY
    }
}

data class HttpHeaders(val headers: List<Pair<String, String>> = listOf()) {
    constructor(vararg items: Pair<String, String>) : this(items.toList())
}

class HttpException(val code: Int, val headers: HttpHeaders = HttpHeaders()) : RuntimeException()

fun redirect(path: String, permanent: Boolean = false): Nothing = throw HttpException(if (permanent) 301 else 302, HttpHeaders("Location" to path))

interface HttpRequest {
    val path: String
    val method: HttpMethod
    val headers: HttpHeaders
    suspend fun retrieveBody(): Any?
}

interface HttpResponse {
    val statusCode: Int
    val statusMessage: String
    val headers: HttpHeaders
    suspend fun retrieveBody(): Any
}

interface HttpServer {
    suspend fun register(path: String, method: HttpMethod = HttpMethod.ANY, handler: suspend (HttpRequest) -> HttpResponse)
}

//////////////////////////////////

class SimpleHttpRequest(
        override val path: String,
        override val method: HttpMethod = HttpMethod.GET,
        override val headers: HttpHeaders = HttpHeaders(),
        val body: Any? = null
) : HttpRequest {
    override suspend fun retrieveBody(): Any? = body
}

class SimpleHttpResponse(
        override val statusCode: Int,
        val body: Any = "",
        override val headers: HttpHeaders = HttpHeaders(),
        override val statusMessage: String = "$statusCode"
) : HttpResponse {
    override suspend fun retrieveBody() = body
}
*/
