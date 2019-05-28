package kuick.caching

interface Invalidable<K : Any> {
    suspend fun invalidate(key: K)
    suspend fun invalidateAll(): Unit = TODO("Invalidable.invalidateAll not implemented")
}
