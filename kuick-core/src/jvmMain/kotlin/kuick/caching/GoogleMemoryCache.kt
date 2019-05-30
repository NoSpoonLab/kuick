package kuick.caching

import com.google.common.cache.CacheBuilder
import kotlinx.coroutines.*
import kuick.core.KuickInternal
import kuick.di.injector
import kuick.di.withInjectorContext
import java.time.Duration
import java.util.concurrent.TimeUnit
import kotlin.coroutines.*

class GoogleMemoryCache<K: Any, T>(maxSize: Long?, duration: Duration?) : Cache<K, T> {
    private val cache = CacheBuilder.newBuilder()
        .apply { if (maxSize != null) maximumSize(maxSize) }
        .apply { if (duration != null) expireAfterWrite(duration.toMillis(), TimeUnit.MILLISECONDS) }
        .build<String, Deferred<T>>()

    @UseExperimental(KuickInternal::class)
    override suspend fun get(key: K, builder: suspend (key: K) -> T): T {
        val context = coroutineContext
        val deferred = cache.get(key.toString()) { CoroutineScope(context).async(context) { builder(key) } }
        return deferred.await()
    }

    override suspend fun invalidate(key: K) = cache.invalidate(key.toString())
    override suspend fun invalidateAll() = cache.invalidateAll()
}
