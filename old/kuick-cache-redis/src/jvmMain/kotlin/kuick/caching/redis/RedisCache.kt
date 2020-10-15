package kuick.caching.redis

import io.lettuce.core.*
import kuick.caching.*
import kuick.client.redis.*
import kuick.json.*
import kotlin.reflect.*

class RedisCache @PublishedApi internal constructor(
        private val redis: RedisSuspendCommands<String, String>,
        private val cacheName: String
) : Cache<String, String> {
    override val name: String get() = cacheName

    companion object {
        suspend operator fun invoke(cacheName: String, uri: RedisURI = RedisURI.create("redis://localhost/"), client: RedisClient = RedisClient.create()): RedisCache {
            return RedisCache(client.connectSuspend(uri).suspending(), cacheName)
        }
    }

    override suspend fun get(key: String, builder: suspend (key: String) -> String): String {
        return redis.hget(cacheName, key) ?: return builder(key).also { redis.hset(cacheName, key, it) }
    }

    override suspend fun invalidate(key: String) {
        redis.hdel(cacheName, key)
    }

    override suspend fun invalidateAll() {
        redis.del(cacheName)
    }
}
