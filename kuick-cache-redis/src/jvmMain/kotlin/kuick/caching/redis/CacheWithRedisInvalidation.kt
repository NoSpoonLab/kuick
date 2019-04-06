package kuick.caching.redis

import kuick.caching.*
import java.io.*

class CacheWithRedisInvalidation<V : Any> @PublishedApi internal constructor(
        val parentCache: Cache<String, V>,
        val cacheName: String,
        val invalidationRedisClient: InvalidationRedisClient
) : Cache<String, V>, Closeable {
    private val register = invalidationRedisClient.register(cacheName) {
        parentCache.invalidate(it)
    }

    override suspend fun get(key: String, builder: suspend () -> V): V {
        return parentCache.get(key, builder)
    }

    override suspend fun invalidate(key: String) {
        invalidationRedisClient.invalidate(cacheName, key)
    }

    override fun close() {
        register.close()
    }
}

fun <V : Any> Cache<String, V>.withRedisInvalidation(cacheName: String, invalidationRedisClient: InvalidationRedisClient) =
        CacheWithRedisInvalidation(this, cacheName, invalidationRedisClient)
