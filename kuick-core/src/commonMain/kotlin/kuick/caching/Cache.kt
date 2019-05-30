package kuick.caching

import kuick.util.*

interface Cache<K : Any, V> : Invalidable<K>, AsyncCloseable, Named {
    override val name get() = "UnnamedCache"
    suspend fun get(key: K, builder: suspend (key: K) -> V): V
    override suspend fun close() = Unit
}

fun <K : Any, V> Cache<K, V>.interceptInvalidation(invalidation: (key: K) -> Unit): Cache<K, V> = object : Cache<K, V> by this {
    override suspend fun invalidate(key: K) {
        invalidation(key)
        return this@interceptInvalidation.invalidate(key)
    }
}
fun <K : Any, V> Cache<K, V>.interceptGet(get: (key: K, value: V) -> V): Cache<K, V> = object : Cache<K, V> by this {
    override suspend fun get(key: K, builder: suspend (key: K) -> V): V = this@interceptGet.get(key) { get(it, builder(it)) }
}
