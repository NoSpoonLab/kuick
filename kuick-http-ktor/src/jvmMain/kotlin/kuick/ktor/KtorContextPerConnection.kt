package kuick.ktor

import com.google.inject.*
import io.ktor.application.*
import kuick.di.*
import kotlin.coroutines.*

fun Application.installContextPerRequest(injector: Injector, context: CoroutineContext.Element, callback: suspend () -> Unit) {
    val perCoroutineJob = injector.get<PerCoroutineJob>()
    perCoroutineJob.registerContext(context)
    perCoroutineJob.runBlocking { callback() }
    install(PerCoroutineJobFeature(injector))
}
