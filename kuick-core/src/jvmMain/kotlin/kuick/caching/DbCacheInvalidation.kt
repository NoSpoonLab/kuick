package kuick.caching

import kotlinx.coroutines.delay
import kotlinx.coroutines.runBlocking
import kuick.concurrent.Lock
import kuick.models.Id
import kuick.repositories.ModelRepository
import kuick.repositories.annotations.DbName
import kuick.repositories.annotations.Index
import kuick.repositories.annotations.MaxLength
import kuick.repositories.annotations.Unique
import kuick.repositories.eq
import kuick.repositories.gt
import kuick.repositories.lt
import java.io.Closeable
import kotlin.concurrent.thread

class DbCacheInvalidation @PublishedApi internal constructor(
    val delay: Long = 5_000L,
    val repo: ModelRepository<CacheInvalidationEntry.CacheId, CacheInvalidationEntry>,
    val debug: Boolean = false,
    val setCoroutineContext: suspend (block: suspend () -> Unit) -> Unit = { it() }
) : Closeable {
    private val lock = Lock()
    private val handlers = LinkedHashMap<String, ArrayList<suspend (key: String) -> Unit>>()
    private var running = true
    private lateinit var process: Thread

    suspend fun init() {
        repo.init()
        process = thread(isDaemon = true, name = "DbCacheInvalidationThread") {
            try {
                runBlocking {
                    setCoroutineContext {
                        var lastTime = now()
                        while (running) {
                            try {
                                //println("getUpdatedSince: $lastTime")
                                val entries = getUpdatedSince(lastTime)
                                if (debug) {
                                    println("getUpdatedSince: $lastTime : ${entries.size} : ${lock { handlers.keys }}")
                                }
                                //println("" + repo.getAll().size + " :: " + entries.size)
                                for (entry in entries) {
                                    val handlers = lock { handlers[entry.cacheName]?.toList() }
                                    if (handlers != null) {
                                        for (handler in handlers) {
                                            handler(entry.key)
                                        }
                                    }
                                }
                                lastTime = entries.lastOrNull()?.invalidationTime ?: lastTime
                            } catch (e: Throwable) {
                                e.printStackTrace()
                            }
                            delay(delay)
                        }
                    }
                }
            } catch (e: Throwable) {
                System.err.println("DbCacheInvalidation.init exception")
                e.printStackTrace()
            }
        }
    }

    companion object {
        /**
         * You must call the .close method whenever this is not required aymore.
         */
        suspend fun getUnsafe(
            delay: Long = 5_000L,
            repo: ModelRepository<CacheInvalidationEntry.CacheId, CacheInvalidationEntry>,
            debug: Boolean = false,
            setCoroutineContext: suspend (block: suspend () -> Unit) -> Unit = { it() }
        ): DbCacheInvalidation {
            return DbCacheInvalidation(delay, repo, debug, setCoroutineContext).apply { init() }
        }

        suspend inline fun <T> get(
            delay: Long = 5_000L,
            repo: ModelRepository<CacheInvalidationEntry.CacheId, CacheInvalidationEntry>,
            debug: Boolean = false,
            noinline setCoroutineContext: suspend (block: suspend () -> Unit) -> Unit = { it() },
            callback: (DbCacheInvalidation) -> T
        ): T {
            return getUnsafe(delay, repo, debug, setCoroutineContext).use { callback(it) }
        }

        private fun now() = System.currentTimeMillis()
    }

    suspend fun invalidate(cacheName: String, key: String) {
        repo.transaction {
            repo.upsert(CacheInvalidationEntry(CacheInvalidationEntry.CacheId("$cacheName:$key"), cacheName, key, now()))
        }
        //repo.insert(CacheInvalidationEntry(cacheName, key, Date()))
    }

    suspend fun invalidateAll(cacheName: String) {
        repo.transaction {
            repo.deleteBy(CacheInvalidationEntry::cacheName eq cacheName)
        }
    }

    fun register(cacheName: String, handler: suspend (key: String) -> Unit): Closeable {
        lock { handlers.getOrPut(cacheName) { arrayListOf() }.add(handler) }
        return Closeable {
            lock { handlers.getOrPut(cacheName) { arrayListOf() }.remove(handler) }
        }
    }

    suspend fun getUpdatedSince(time: Long) =
        repo.transaction { repo.findBy(CacheInvalidationEntry::invalidationTime gt time).sortedBy { it.invalidationTime } }

    suspend fun pruneBefore(time: Long) = repo.transaction { repo.deleteBy(CacheInvalidationEntry::invalidationTime lt time) }

    override fun close() {
        running = false
        //process.stop()
    }

    @DbName("CacheInvalidationEntry")
    data class CacheInvalidationEntry(
        @Unique @MaxLength(128) val cacheNameKey: CacheId,
        @MaxLength(64) val cacheName: String,
        @MaxLength(64) val key: String,
        @Index val invalidationTime: Long
    ) {
        data class CacheId(override val id: String) : Id
    }
}

suspend fun <T> Cache<String, T>.withInvalidationDb(
    delay: Long = 5_000L,
    repo: ModelRepository<DbCacheInvalidation.CacheInvalidationEntry.CacheId, DbCacheInvalidation.CacheInvalidationEntry>,
    debug: Boolean = false,
    setCoroutineContext: suspend (block: suspend () -> Unit) -> Unit = { it() }
): Cache<String, T> = withInvalidation(DbCacheInvalidation(delay, repo, debug, setCoroutineContext))

fun <T> Cache<String, T>.withInvalidation(dbCacheInvalidation: DbCacheInvalidation): Cache<String, T> {
    val parent = this
    val cacheName = this.name

    val closeable = dbCacheInvalidation.register(cacheName) {
        parent.invalidate(it)
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
