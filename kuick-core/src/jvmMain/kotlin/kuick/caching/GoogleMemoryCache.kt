package kuick.caching

import com.google.common.cache.CacheBuilder
import kotlinx.coroutines.runBlocking
import kuick.core.KuickInternal
import kuick.di.injector
import kuick.di.withInjectorContext
import java.time.Duration
import java.util.concurrent.TimeUnit

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
