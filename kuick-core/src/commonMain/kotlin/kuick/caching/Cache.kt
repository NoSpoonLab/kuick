package kuick.caching

import kuick.util.*

interface Cache<K : Any, T : Any> : Invalidable<K>, AsyncCloseable {
    suspend fun get(key: K, builder: suspend () -> T): T
    override suspend fun invalidate(key: K)
    override suspend fun close() = Unit
}
