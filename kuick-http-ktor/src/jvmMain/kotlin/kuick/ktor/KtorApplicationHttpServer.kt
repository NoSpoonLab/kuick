package kuick.ktor

/*
import io.ktor.application.*
import io.ktor.http.*
import io.ktor.request.*
import io.ktor.response.*
import io.ktor.routing.*
import kotlinx.coroutines.io.*
import kotlinx.io.core.*
import kuick.http.*
import kuick.http.HttpMethod

class KtorApplicationHttpServer(val app: Application) : HttpServer {
    override suspend fun register(path: String, method: HttpMethod, handler: suspend (HttpRequest) -> HttpResponse) {
        app.routing {
            route(path, method.toKtor()) {
                handle {
                    val response = handler(call.request.toKuick())
                    for ((key, value) in response.headers.headers) {
                        call.response.header(key, value)
                    }
                    call.respond(HttpStatusCode.fromValue(response.statusCode), response.retrieveBody())
                }
            }
        }
    }

    fun HttpMethod.toKtor() = io.ktor.http.HttpMethod.parse(this.name)
    fun io.ktor.http.HttpMethod.toKuick() = HttpMethod.parse(this.value)

    fun io.ktor.http.Headers.toKuick() = kuick.http.HttpHeaders(this.entries().flatMap { pair -> pair.value.map { pair.key to it } })

    suspend fun ApplicationRequest.toKuick(): HttpRequest {
        return SimpleHttpRequest(this.path(), this.httpMethod.toKuick(), this.headers.toKuick(), this.receiveChannel().readRemaining().readBytes())
    }
}
*/
