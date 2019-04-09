package kuick.ktor

import io.ktor.application.*
import io.ktor.content.*
import io.ktor.http.*
import io.ktor.request.*
import io.ktor.util.*
import io.ktor.util.pipeline.*

fun PipelineContext<*, ApplicationCall>.header(name: String): String = headerOrNull(name)
        ?: error("Can't find header '$name'")

fun PipelineContext<*, ApplicationCall>.param(name: String): String = paramOrNull(name)
        ?: error("Can't find url parameter '$name'")

fun PipelineContext<*, ApplicationCall>.get(name: String): String = getOrNull(name)
        ?: error("Can't find get parameter '$name'")

suspend fun PipelineContext<*, ApplicationCall>.post(name: String): String = postOrNull(name)
        ?: error("Can't find post parameter '$name'")

fun PipelineContext<*, ApplicationCall>.headerOrNull(name: String): String? = call.request.header(name)
fun PipelineContext<*, ApplicationCall>.paramOrNull(name: String): String? = call.parameters[name]
fun PipelineContext<*, ApplicationCall>.getOrNull(name: String): String? = call.request.queryParameters[name]

suspend fun PipelineContext<*, ApplicationCall>.postOrNull(name: String): String? {
    val params = call.attributes.getOrPut(BodyParametersAttribute) { call.receiveParameters() }
    return params[name]
}

inline fun <T : Any> Attributes.getOrPut(key: AttributeKey<T>, compute: () -> T): T =
        getOrNull(key) ?: compute().also { put(key, it) }

private val BodyParametersAttribute = AttributeKey<Parameters>("BodyParametersAttribute")

interface SuspendingResult<T : Any> {
    suspend fun get(): T
}

suspend fun kuickProcessResult(result: Any): Any {
    var cresult = result
    if (cresult is SuspendingResult<*>) cresult = cresult.get()
    if (cresult is String) cresult = TextContent(cresult, ContentType.Text.Html)
    return cresult
}
