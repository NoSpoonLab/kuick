package kuick.caching

import kotlinx.coroutines.runBlocking
import kotlin.test.Test
import kotlin.test.assertEquals

class LazyCacheTest {
    @Test
    fun test() = runBlocking {
        var log = ""
        val cache = LazyCache {
            log += "a"
            GoogleMemoryCache<String, String>()
        }
        assertEquals("", log)
        cache.name
        assertEquals("a", log)
        cache.get("a") { "a" }
        assertEquals("a", log)
    }
}