package kuick.ktor

import io.ktor.application.*
import io.ktor.content.*
import io.ktor.http.*
import io.ktor.response.*
import io.ktor.routing.*
import io.ktor.util.pipeline.*

fun String.withContentType(contentType: ContentType, statusCode: HttpStatusCode? = null): TextContent =
        TextContent(this, contentType, statusCode)

class KuickRouting(val routing: Routing) {
}

fun Application.kuickRouting(configuration: KuickRouting.() -> Unit): KuickRouting =
        KuickRouting(routing { }).apply(configuration)

fun KuickRouting.get(path: String, callback: suspend PipelineContext<Unit, ApplicationCall>.() -> Any) = routing.get(path) { call.respond(kuickProcessResult(callback())) }
fun KuickRouting.post(path: String, callback: suspend PipelineContext<Unit, ApplicationCall>.() -> Any) = routing.post(path) { call.respond(kuickProcessResult(callback())) }
