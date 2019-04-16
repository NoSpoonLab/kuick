package kuick.caching

interface Invalidable<K : Any> {
    suspend fun invalidate(key: K)
}
