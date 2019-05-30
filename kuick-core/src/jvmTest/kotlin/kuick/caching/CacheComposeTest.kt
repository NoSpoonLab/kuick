package kuick.caching

import kotlinx.coroutines.runBlocking
import kotlin.test.Test
import kotlin.test.assertEquals

class CacheComposeTest {
    @Test
    fun test() = runBlocking {
        val log = arrayListOf<String>()
        val first = GoogleMemoryCache<String, String>("first", maxSize = 1L).interceptGet { log += "1[$it]" }
        val second = GoogleMemoryCache<String, String>("second", maxSize = 1L).interceptGet { log += "2[$it]" }
        val cache = first.withFallback(second).withBuilder { it }

        cache.get("a")
        cache.get("a")
        assertEquals("a", cache.get("a"))

        assertEquals("1[a], 2[a], 1[a], 1[a]", log.joinToString(", "))
    }
}