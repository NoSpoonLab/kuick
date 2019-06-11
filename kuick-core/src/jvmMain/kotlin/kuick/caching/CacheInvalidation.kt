package kuick.caching

import kuick.event.SuspendEventHandler
import java.io.Closeable

interface CacheInvalidation : Closeable {
    companion object {
        //val INVALIDATE_ALL_KEY = ""
        val INVALIDATE_ALL_KEY = "*"
        //val INVALIDATE_ALL_KEY = "\u0000"
    }

    fun register(cacheName: String, handler: suspend (key: String) -> Unit): Closeable
    suspend fun invalidate(cacheName: String, key: String)
    suspend fun invalidateAll(cacheName: String) = invalidate(cacheName, INVALIDATE_ALL_KEY)
}

class InmemoryCacheInvalidation : CacheInvalidation {
    private val events = SuspendEventHandler<String, String>()

    override fun register(cacheName: String, handler: suspend (key: String) -> Unit): Closeable {
        return events.register(cacheName, handler)
    }

    override suspend fun invalidate(cacheName: String, key: String) {
        events.dispatch(cacheName, key)
    }

    override fun close() {
        events.clear()
    }
}

fun <T> Cache<String, T>.withInvalidation(dbCacheInvalidation: CacheInvalidation): Cache<String, T> {
    val parent = this
    val cacheName = this.name

    val closeable = dbCacheInvalidation.register(cacheName) {
        if (it == CacheInvalidation.INVALIDATE_ALL_KEY) {
            parent.invalidateAll()
        } else {
            parent.invalidate(it)
        }
    }

    return object : Cache<String, T> {
        override suspend fun get(key: String, builder: suspend (key: String) -> T): T = parent.get(key, builder)
        override suspend fun invalidate(key: String) = run { dbCacheInvalidation.invalidate(cacheName, key) }
        override suspend fun invalidateAll() = run { dbCacheInvalidation.invalidateAll(cacheName) }

        override suspend fun close() {
            parent.close()
            @Suppress("BlockingMethodInNonBlockingContext")
            closeable.close()
        }
    }
}
