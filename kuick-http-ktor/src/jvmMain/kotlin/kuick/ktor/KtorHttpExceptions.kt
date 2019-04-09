package kuick.ktor

import io.ktor.application.*
import io.ktor.features.*
import io.ktor.http.*
import io.ktor.response.*
import io.ktor.util.pipeline.*

open class HttpException(val code: HttpStatusCode, val headers: Headers, val body: Any?) : RuntimeException("$code")

open class HttpRedirectException(val location: String, val permanent: Boolean) : HttpException(
        if (permanent) HttpStatusCode.PermanentRedirect else HttpStatusCode.Found,
        Headers.build { append("Location", location) },
        null
)

fun PipelineContext<*, ApplicationCall>.httpError(code: HttpStatusCode, headers: Headers, body: Any? = null): Nothing =
        throw HttpException(code, headers, body)

fun PipelineContext<*, ApplicationCall>.redirect(location: String, permanent: Boolean = false): Nothing =
        throw HttpRedirectException(location, permanent)

fun Application.installHttpExceptionsSupport() {
    install(StatusPages) {
        exception<HttpException> { e ->
            for ((k, list) in e.headers.entries()) {
                for (v in list) call.response.header(k, v)
            }
            call.respond(e.code, kuickProcessResult(e.body ?: ""))
        }
    }
}
