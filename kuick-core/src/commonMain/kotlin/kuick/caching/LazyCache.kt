package kuick.caching

import kuick.concurrent.Lock

class LazyCache<K : Any, V>(val get: () -> Cache<K, V>) : Cache<K, V> {
    private val lock = Lock()
    private var _cache: Cache<K, V>? = null
    private val cache: Cache<K, V>
        get() = lock {
            if (_cache == null) _cache = get()
            return _cache!!
        }

    override val name: String get() = cache.name
    override suspend fun get(key: K, builder: suspend (key: K) -> V): V = cache.get(key, builder)
    override suspend fun close() = cache.close()
    override suspend fun invalidate(key: K) = cache.invalidate(key)
    override suspend fun invalidateAll() = cache.invalidateAll()
}
