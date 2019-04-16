package kuick.caching

import kuick.util.*

interface Cache<K : Any, T : Any> : Invalidable<K>, AsyncCloseable {
    suspend fun get(key: K, builder: suspend () -> T): T
    override suspend fun invalidate(key: K)
    override suspend fun close() = Unit
}

fun <K : Any, T : Any> Cache<K, T>.interceptInvalidation(name: String? = null, invalidation: (key: K) -> Unit): Cache<K, T> = object : Cache<K, T> by this, Named {
    override val name: String get() = name ?: (this@interceptInvalidation as Named).name

    override suspend fun invalidate(key: K) {
        invalidation(key)
        return this@interceptInvalidation.invalidate(key)
    }
}