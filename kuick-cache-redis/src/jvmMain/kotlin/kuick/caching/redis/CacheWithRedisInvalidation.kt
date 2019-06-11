package kuick.caching.redis

import kuick.caching.*
import java.io.*

@Deprecated("Review CacheInvalidation and Cache<String, T>.withInvalidation")
class CacheWithRedisInvalidation<V> @PublishedApi internal constructor(
        val parentCache: Cache<String, V>,
        val cacheName: String,
        val invalidationRedisClient: InvalidationRedisClient
) : Cache<String, V> {
    override val name: String get() = cacheName

    private val register = invalidationRedisClient.register(cacheName) {
        if (it == CacheInvalidation.INVALIDATE_ALL_KEY) {
            parentCache.invalidateAll()
        } else {
            parentCache.invalidate(it)
        }
    }

    override suspend fun get(key: String, builder: suspend (key: String) -> V): V {
        return parentCache.get(key, builder)
    }

    override suspend fun invalidate(key: String) {
        invalidationRedisClient.invalidate(cacheName, key)
    }

    override suspend fun invalidateAll() {
        invalidationRedisClient.invalidateAll(cacheName)
    }

    override suspend fun close() {
        register.close()
    }
}

@Deprecated("Review CacheInvalidation and Cache<String, T>.withInvalidation")
fun <V> Cache<String, V>.withInvalidation(cacheName: String, invalidationRedisClient: InvalidationRedisClient) =
        CacheWithRedisInvalidation(this, cacheName, invalidationRedisClient)
