package kuick.caching

import kuick.util.AsyncCloseable
import kuick.util.Named

class CacheWithBuilder<K : Any, V : Any>(val cache: Cache<K, V>, val builder: suspend (key: K) -> V) : Invalidable<K> by cache, AsyncCloseable by cache, Named by cache {
    suspend fun get(key: K): V = cache.get(key) { builder(key) }
}

fun <K : Any, V : Any> Cache<K, V>.withBuilder(builder: suspend (key: K) -> V) = CacheWithBuilder(this, builder)
