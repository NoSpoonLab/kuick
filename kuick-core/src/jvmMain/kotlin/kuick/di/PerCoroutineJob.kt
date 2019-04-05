package kuick.di

import com.google.inject.*
import kuick.concurrent.atomic.*
import java.util.concurrent.atomic.*

@Singleton
class PerCoroutineJob {
    var handlers by AtomicReference(listOf<PerCoroutineJobHandler>())

    fun register(handler: PerCoroutineJobHandler) {
        handlers = handlers + handler
    }

    fun runBlocking(callback: suspend () -> Unit) = kotlinx.coroutines.runBlocking { runSuspending(callback) }

    suspend fun runSuspending(callback: suspend () -> Unit) {
        executeChunk(handlers, 0, callback)
    }

    private suspend fun executeChunk(handlers: List<PerCoroutineJobHandler>, index: Int, callback: suspend () -> Unit) {
        if (index < handlers.size) {
            handlers[index].invoke { executeChunk(handlers, index + 1, callback) }
        } else {
            callback()
        }
    }
}

fun Binder.bindPerCoroutineJob() = bindSelf<PerCoroutineJob>()

typealias PerCoroutineJobHandler = suspend (callback: suspend () -> Unit) -> Unit
