package kuick.caching

fun <K : Any, V> Cache<K, V>.withFallback(second: Cache<K, V>): Cache<K, V> {
    val first = this
    return object : Cache<K, V> {
        override suspend fun get(key: K, builder: suspend (key: K) -> V): V = first.get(key) { second.get(key, builder) }

        override suspend fun invalidate(key: K) {
            second.invalidate(key)
            first.invalidate(key)
        }

        override suspend fun invalidateAll() {
            second.invalidateAll()
            first.invalidateAll()
        }
    }
}

fun <K : Any, V> Cache<K, V>.compose(first: Cache<K, V>): Cache<K, V> = first.withFallback(this)
