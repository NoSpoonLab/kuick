package kuick.http

/*
class TestHttpServer : HttpServer {
    val paths = LinkedHashMap<String, LinkedHashMap<HttpMethod, suspend (HttpRequest) -> HttpResponse>>()

    override suspend fun register(path: String, method: HttpMethod, handler: suspend (HttpRequest) -> HttpResponse) {
        paths.getOrPut(path) { LinkedHashMap() }[method] = handler
    }

    suspend fun request(path: String, headers: HttpHeaders = HttpHeaders(), body: Any? = null, statusCode: Int = 200, method: HttpMethod = HttpMethod.GET): HttpResponse? {
        val mpath = paths[path]
        val func = mpath?.get(method) ?: mpath?.get(HttpMethod.ANY)
        return func?.invoke(SimpleHttpRequest(path, method, headers, body))
    }
}
*/
