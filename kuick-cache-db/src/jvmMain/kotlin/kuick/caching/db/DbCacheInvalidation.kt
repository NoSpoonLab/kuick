package kuick.caching.db

import kotlinx.coroutines.*
import kuick.caching.*
import kuick.client.repositories.*
import kuick.concurrent.*
import kuick.repositories.*
import kuick.repositories.annotations.*
import kuick.util.*
import java.io.*
import java.util.*
import kotlin.collections.*
import kotlin.coroutines.*

class DbCacheInvalidation(val coroutineContext: CoroutineContext, val delay: Long = 5_000L, val repo: ModelRepository<String, CacheInvalidationEntry> = CacheInvalidationEntry) : Closeable {
    private val lock = Lock()
    private val handlers = LinkedHashMap<String, ArrayList<suspend (key: String) -> Unit>>()
    private val process = CoroutineScope(coroutineContext).launch {
        var lastTime = Date()
        while (true) {
            val entries = getUpdatedSince(lastTime)
            for (entry in entries) {
                val handlers = lock { handlers[entry.cacheName]?.toList() }
                if (handlers != null) {
                    for (handler in handlers) {
                        handler(entry.key)
                    }
                }
            }
            lastTime = entries.lastOrNull()?.invalidationTime ?: lastTime
            delay(delay)
        }
    }

    companion object {
        suspend inline fun <T> get(delay: Long = 5_000L, repo: ModelRepository<String, CacheInvalidationEntry> = CacheInvalidationEntry, callback: suspend (DbCacheInvalidation) -> T): T =
                DbCacheInvalidation(coroutineContext, delay, repo).use { callback(it) }
    }

    suspend fun invalidate(cacheName: String, key: String) {
        repo.upsert(CacheInvalidationEntry(cacheName, key, Date()))
    }

    fun register(cacheName: String, handler: suspend (key: String) -> Unit): Closeable {
        lock { handlers.getOrPut(cacheName) { arrayListOf() }.add(handler) }
        return Closeable {
            lock { handlers.getOrPut(cacheName) { arrayListOf() }.remove(handler) }
        }
    }

    suspend fun getUpdatedSince(time: Date) = repo.findBy(CacheInvalidationEntry::invalidationTime gt time).sortedBy { it.invalidationTime }
    suspend fun pruneBefore(time: Date) = repo.deleteBy(CacheInvalidationEntry::invalidationTime lt time)

    override fun close() {
        process.cancel()
    }

    @DbName("CacheInvalidationEntry")
    data class CacheInvalidationEntry(
            val cacheName: String,
            val key: String,
            @Index val invalidationTime: Date
    ) {
        companion object : ModelRepository<String, CacheInvalidationEntry> by DbModelRepository(CacheInvalidationEntry::key)
    }
}

fun <T : Any> Cache<String, T>.invalidatedBy(dbCacheInvalidation: DbCacheInvalidation, cacheName: String = (this as Named).name): Cache<String, T> {
    val parent = this

    val closeable = dbCacheInvalidation.register(cacheName) {
        parent.invalidate(it)
    }

    return object : Cache<String, T> {
        override suspend fun get(key: String, builder: suspend () -> T): T = parent.get(key, builder)
        override suspend fun invalidate(key: String) = run { dbCacheInvalidation.invalidate(cacheName, key) }
        override suspend fun close() {
            parent.close()
            @Suppress("BlockingMethodInNonBlockingContext")
            closeable.close()
        }
    }
}
