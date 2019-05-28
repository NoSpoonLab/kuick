package kuick.caching.redis

import io.lettuce.core.*
import kuick.caching.*
import kuick.client.redis.*
import kuick.json.*
import kotlin.reflect.*

class RedisCache<V : Any> @PublishedApi internal constructor(
        private val redis: RedisSuspendCommands<String, String>,
        private val cacheName: String,
        private val clazz: KClass<V>
) : Cache<String, V> {
    override val name: String get() = cacheName

    companion object {
        suspend inline operator fun <reified V : Any> invoke(cacheName: String, uri: RedisURI = RedisURI.create("redis://localhost/"), client: RedisClient = RedisClient.create()): RedisCache<V> {
            return RedisCache(client.connectSuspend(uri).suspending(), cacheName, V::class)
        }
    }

    override suspend fun get(key: String, builder: suspend (key: String) -> V): V {
        val value = redis.hget(cacheName, key) ?: return builder(key).also { redis.hset(cacheName, key, Json.toJson(it)) }
        return Json.fromJson(value, clazz)
    }

    override suspend fun invalidate(key: String) {
        redis.hdel(cacheName, key)
    }
}
