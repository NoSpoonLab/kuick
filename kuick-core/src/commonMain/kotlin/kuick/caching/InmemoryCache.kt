package kuick.caching

import kotlinx.coroutines.*
import kuick.concurrent.*
import kotlin.coroutines.*

class InmemoryCache<K : Any, V>(override val name: String = "InmemoryCache") : Cache<K, V> {
    private val lock = Lock()
    private val map = LinkedHashMap<K, Deferred<V>>()

    override suspend fun get(key: K, builder: suspend (key: K) -> V): V {
        val context = coroutineContext
        return lock { map.getOrPut(key) { CoroutineScope(context).async { builder(key) } } }.await()
    }

    override suspend fun invalidate(key: K): Unit = lock { map.remove(key) }
    override suspend fun invalidateAll() = lock { map.clear() }
}
