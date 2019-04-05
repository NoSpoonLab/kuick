package kuick.di

import com.google.inject.*
import kotlinx.coroutines.*
import kuick.concurrent.atomic.*
import kuick.utils.*
import java.util.concurrent.atomic.*

@Singleton
class PerCoroutineJob @Inject constructor(val injector: Injector) {
    var handlers by AtomicReference(listOf<PerCoroutineJobHandler>())

    fun register(handler: PerCoroutineJobHandler): PerCoroutineJob {
        handlers = handlers + handler
        return this
    }

    fun runBlocking(callback: suspend () -> Unit) = kotlinx.coroutines.runBlocking { runSuspending(callback) }
    suspend fun runSuspending(callback: suspend () -> Unit) = executeChunk(handlers, 0, callback)

    private suspend fun executeChunk(handlers: List<PerCoroutineJobHandler>, index: Int, callback: suspend () -> Unit) {
        if (index < handlers.size) handlers[index].invoke { executeChunk(handlers, index + 1, callback) } else callback()
    }
}

fun Binder.bindPerCoroutineJob() = bindSelf<PerCoroutineJob>()

typealias PerCoroutineJobHandler = suspend (callback: suspend () -> Unit) -> Unit

//fun Injector.registerPerCoroutineJobInjectorInContext() {
//    val injector = this
//    injector.get<PerCoroutineJob>().register { callback ->
//        withInjectorContext(injector) { callback() }
//    }
//}

suspend fun <T> Injector.runWithInjector(callback: suspend () -> T): T {
    val injector = this
    val result = ObjContainer<T?>(null)
    withContext(InjectorContext(injector)) {
        injector.get<PerCoroutineJob>().runSuspending {
            result.instance = callback()
        }
    }
    @Suppress("UNCHECKED_CAST")
    return (result as ObjContainer<T>).instance
}

fun <T> Injector.runBlockingWithInjector(callback: suspend () -> T): T = runBlocking { runWithInjector(callback) }
