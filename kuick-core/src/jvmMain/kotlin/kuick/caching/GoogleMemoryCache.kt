package kuick.caching

import com.google.common.cache.CacheBuilder
import kotlinx.coroutines.*
import kuick.core.KuickInternal
import java.time.Duration
import java.util.concurrent.TimeUnit
import kotlin.coroutines.*

class GoogleMemoryCache<K: Any, V>(maxSize: Long? = null, duration: Duration? = null) : Cache<K, V> {
    private val cache = CacheBuilder.newBuilder()
        .apply { if (maxSize != null) maximumSize(maxSize) }
        .apply { if (duration != null) expireAfterWrite(duration.toMillis(), TimeUnit.MILLISECONDS) }
        .build<String, Deferred<V>>()

    @UseExperimental(KuickInternal::class)
    override suspend fun get(key: K, builder: suspend (key: K) -> V): V {
        val context = coroutineContext
        val deferred = cache.get(key.toString()) { CoroutineScope(context).async(context) { builder(key) } }
        return deferred.await()
    }

    override suspend fun invalidate(key: K) = cache.invalidate(key.toString())
    override suspend fun invalidateAll() = cache.invalidateAll()
}

fun <K : Any, V> Cache<K, V>.withLRUInmemoryCache(maxSize: Long?): Cache<K, V> {
    return GoogleMemoryCache<K, V>(maxSize = maxSize).withFallback(this)
}
