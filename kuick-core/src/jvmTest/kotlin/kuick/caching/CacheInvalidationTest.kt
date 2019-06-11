package kuick.caching

import kotlinx.coroutines.runBlocking
import kuick.repositories.patterns.MemoryCache
import kotlin.test.Test
import kotlin.test.assertEquals

class CacheInvalidationTest {
    @Test
    fun testInmemory() {
        runBlocking {
            val log = arrayListOf<String>()
            val invalidation = InmemoryCacheInvalidation()
            val closeable = invalidation.register("test") { log += it }
            invalidation.invalidate("test", "a")
            invalidation.invalidateAll("test")
            invalidation.invalidate("test2", "b")
            invalidation.invalidateAll("test2")
            invalidation.invalidate("test", "c")
            invalidation.invalidateAll("test")
            closeable.close()
            invalidation.invalidate("test", "d")
            invalidation.invalidateAll("test")
            val all = CacheInvalidation.INVALIDATE_ALL_KEY
            assertEquals("a,$all,c,$all", log.joinToString(","))
        }
    }

    @Test
    fun testDecorator() {
        runBlocking {
            val log = arrayListOf<String>()
            val invalidation = InmemoryCacheInvalidation()
            val cacheName = "name"
            val otherCacheName = "other"
            val all = "<ALL>"
            val cache = GoogleMemoryCache<String, String>(cacheName)
                .interceptInvalidation { log += it }
                .interceptInvalidationAll { log += all }
                .withInvalidation(invalidation)
                .withBuilder { it }
            cache.get("test")
            cache.get("test")
            invalidation.invalidate(cacheName, "test")
            invalidation.invalidateAll(cacheName)
            invalidation.invalidate(otherCacheName, "test")
            invalidation.invalidateAll(otherCacheName)
            invalidation.invalidate(cacheName, "test2")
            invalidation.invalidateAll(cacheName)
            assertEquals("test,$all,test2,$all", log.joinToString(","))
        }
    }
}