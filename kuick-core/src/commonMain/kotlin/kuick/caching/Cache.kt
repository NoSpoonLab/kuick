package kuick.caching


interface Cache<K : Any, T : Any> {
    suspend fun get(key: K, builder: suspend () -> T): T
    suspend fun invalidate(key: K)
}
