package kuick.caching

import com.google.common.cache.CacheBuilder
import kotlinx.coroutines.*
import kuick.core.KuickInternal
import kuick.di.injector
import kuick.di.withInjectorContext
import java.time.Duration
import java.util.concurrent.TimeUnit
import kotlin.coroutines.*

class GoogleMemoryCache<K: Any, T:Any>(maxSize: Long, duration: Duration) : Cache<K, T> {

    private val cache = CacheBuilder.newBuilder()
            .maximumSize(maxSize)
            .expireAfterWrite(duration.seconds, TimeUnit.SECONDS)
            .build<String, T>()

    @UseExperimental(KuickInternal::class)
    override suspend fun get(key: K, builder: suspend () -> T): T {
        val injector = injector()
        return cache.get(key.toString()) {
            runBlocking {
                withInjectorContext(injector) {
                    builder()
                }
            }
        }
    }

    override suspend fun invalidate(key: K) = cache.invalidate(key.toString())

}

class GoogleMemoryCache2<K: Any, T:Any>(maxSize: Long, duration: Duration) : Cache<K, T> {
    private val cache = CacheBuilder.newBuilder()
            .maximumSize(maxSize)
            .expireAfterWrite(duration.seconds, TimeUnit.SECONDS)
            .build<String, Deferred<T>>()

    @UseExperimental(KuickInternal::class)
    // @TODO: Shouldn't this be? Can't return T if we produce T?: suspend fun get(key: K, builder: suspend () -> T): T
    override suspend fun get(key: K, builder: suspend () -> T): T {
        val context = coroutineContext
        val deferred = cache.get(key.toString()) { CoroutineScope(context).async(context) { builder() } }
        return deferred.await()
    }

    override suspend fun invalidate(key: K) = cache.invalidate(key.toString())
}
