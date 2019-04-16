package kuick.concurrent.async

import kotlinx.coroutines.*
import kuick.concurrent.*
import kotlin.coroutines.*

interface AsyncInvokable {
    suspend operator fun <T> invoke(func: suspend () -> T): T
}

/**
 * Creates a queue of processes that will be executed one after another by effectively preventing from executing
 * them at the same time.
 * This class is thread-safe.
 */
class AsyncThread : AsyncInvokable {
    private val lock = Lock()
    private var lastPromise: Deferred<*> = CompletableDeferred(Unit)

    suspend fun await() {
        while (true) {
            val cpromise = lock { lastPromise }
            cpromise.await()
            if (lock { cpromise == lastPromise }) break
        }
    }

    fun cancel(): AsyncThread {
        lock { lastPromise }.cancel()
        lock { lastPromise = CompletableDeferred(Unit) }
        return this
    }

    override suspend operator fun <T> invoke(func: suspend () -> T): T {
        val task = invoke(coroutineContext, func)
        try {
            val res = task.await()
            return res
        } catch (e: Throwable) {
            throw e
        }
    }

    private operator fun <T> invoke(context: CoroutineContext, func: suspend () -> T): Deferred<T> = lock {
        val oldPromise = lastPromise
        CoroutineScope(context).async {
            oldPromise.await()
            func()
        }.also { lastPromise = it }
    }
}

/**
 * Prevents a named invoke to happen at the same time (by effectively enqueuing by name).
 * This class is thread-safe.
 */
class NamedAsyncThreads(val threadFactory: () -> AsyncInvokable = { AsyncThread() }) {
    class AsyncJob(val thread: AsyncInvokable) {
        var count = 0
    }
    private val lock = Lock()
    private val jobs = LinkedHashMap<String, AsyncJob>()

    internal fun threadsCount() = jobs.size

    suspend operator fun <T> invoke(name: String, func: suspend () -> T): T {
        val job = lock {
            jobs.getOrPut(name) { AsyncJob(threadFactory()) }.also { it.count++ }
        }
        try {
            return job.thread.invoke(func)
        } finally {
            // Synchronization to prevent another thread from being added in the mean time, or a process queued.
            lock {
                job.count--
                if (job.count == 0) {
                    jobs.remove(name)
                }
            }
        }
    }
}
