package kuick.di

import com.google.inject.*
import kuick.concurrent.atomic.*
import java.util.concurrent.atomic.*

@Singleton
class PerCoroutineJobService {
    var handlers by AtomicReference(listOf<PerCoroutineJobServiceHandler>())

    fun register(handler: PerCoroutineJobServiceHandler) {
        handlers = handlers + handler
    }

    suspend fun execute(callback: suspend () -> Unit) {
        executeChunk(handlers, 0, callback)
    }

    private suspend fun executeChunk(handlers: List<PerCoroutineJobServiceHandler>, index: Int, callback: suspend () -> Unit) {
        if (index < handlers.size) {
            handlers[index].invoke { executeChunk(handlers, index + 1, callback) }
        } else {
            callback()
        }
    }
}

fun Binder.bindPerCoroutineJobService() = bindSelf<PerCoroutineJobService>()

typealias PerCoroutineJobServiceHandler = suspend (callback: suspend () -> Unit) -> Unit
