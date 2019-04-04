package kuick.caching.redis

import io.lettuce.core.*
import io.lettuce.core.api.*
import io.lettuce.core.pubsub.*
import kotlinx.coroutines.*
import kuick.caching.*
import kuick.client.redis.*
import java.io.*
import kotlin.coroutines.*

class CacheWithRedisInvalidation<V : Any> @PublishedApi internal constructor(
        val parentCache: Cache<String, V>,
        val cacheName: String,
        val cacheRedisClient: CacheRedisClient
) : Cache<String, V>, Closeable {
    private val register = cacheRedisClient.register(cacheName) {
        parentCache.invalidate(it)
    }

    override suspend fun get(key: String, builder: suspend () -> V): V {
        return parentCache.get(key, builder)
    }

    override suspend fun invalidate(key: String) {
        cacheRedisClient.invalidate(cacheName, key)
    }

    override fun close() {
        register.close()
    }
}

fun <V : Any> Cache<String, V>.withRedisInvalidation(cacheName: String, cacheRedisClient: CacheRedisClient) =
        CacheWithRedisInvalidation(this, cacheName, cacheRedisClient)
