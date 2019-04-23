package kuick.caching

import kotlinx.coroutines.*
import kuick.concurrent.*
import kuick.util.*
import kotlin.coroutines.*

class InmemoryCache<K : Any, V : Any>(override val name: String = "InmemoryCache") : Cache<K, V>, Named {
    private val lock = Lock()
    private val map = LinkedHashMap<K, Deferred<V>>()

    override suspend fun get(key: K, builder: suspend () -> V): V {
        val context = coroutineContext
        return lock { map.getOrPut(key) { CoroutineScope(context).async { builder() } } }.await()
    }

    override suspend fun invalidate(key: K) {
        lock { map.remove(key) }
    }
}