package kuick.caching.redis

import io.lettuce.core.*
import io.lettuce.core.api.*
import io.lettuce.core.pubsub.*
import kotlinx.coroutines.*
import kuick.caching.CacheInvalidation
import kuick.client.redis.*
import java.io.*
import kotlin.coroutines.*

class InvalidationRedisClient internal constructor(
        private val coroutineContext: CoroutineContext,
        private val redisSub: StatefulRedisPubSubConnection<String, String>,
        private val redisPub: StatefulRedisConnection<String, String>
) : CacheInvalidation {
    private val sredis = redisPub.suspending()

    private val map = LinkedHashMap<String, ArrayList<suspend (String) -> Unit>>()

    override fun register(cacheName: String, handler: suspend (String) -> Unit): Closeable {
        val channel = getChannelForKey(cacheName)
        val arrayList = synchronized(map) {
            map.getOrPut(channel) { arrayListOf() }.also {
                it += handler
            }
        }
        return Closeable { synchronized(map) { arrayList -= handler } }
    }

    override suspend fun invalidate(cacheName: String, key: String) {
        sredis.publish(getChannelForKey(cacheName), key)
    }

    override suspend fun invalidateAll(cacheName: String) {
        sredis.publish(getChannelForKey(cacheName), CacheInvalidation.INVALIDATE_ALL_KEY)
    }

    override fun close() {
        redisSub.closeAsync()
        redisPub.closeAsync()
    }

    companion object {
        private fun getChannelForKey(name: String) = "cache-invalidate-$name"

        suspend operator fun invoke(uri: String = "redis://localhost"): InvalidationRedisClient {
            val clientSub = RedisClient.create()
            val clientPub = RedisClient.create()
            val redisUri = RedisURI.create(uri)
            val redisSub = clientSub.connectPubSubSuspend(redisUri)
            val redisPub = clientPub.connectSuspend(redisUri)
            val context = coroutineContext
            val cacheClient = InvalidationRedisClient(coroutineContext, redisSub, redisPub)
            redisSub.suspending().psubscribe(getChannelForKey("*"))
            redisSub.addListener(object : RedisPubSubAdapter<String, String>() {
                override fun message(pattern: String, channel: String, message: String) {
                    //println("RECEIVED pattern=$pattern, channel=$channel, message=$message")
                    CoroutineScope(context).launch {
                        val callbacks = synchronized(cacheClient.map) { cacheClient.map[channel]?.toList() ?: listOf() }
                        for (callback in callbacks) callback(message)
                    }
                }
            })
            return cacheClient
        }
    }
}
