package kuick.caching

import kotlinx.coroutines.runBlocking
import kuick.time.Clock
import kotlin.test.Test
import kotlin.test.assertEquals

class CacheTtlTest {
    @Test
    fun testMaxEntries() = runBlocking {
        val removeLog = arrayListOf<String>()
        val cache = InmemoryCache<String, String>()
                .withTtl(maxEntries = 2, onRemove = { removeLog += it })
                .withBuilder { it }

        cache.get("a")
        cache.get("b")
        assertEquals(listOf<String>(), removeLog)
        cache.get("c")
        assertEquals(listOf("a"), removeLog)
        cache.get("c")
        assertEquals(listOf("a"), removeLog)
        cache.get("d")
        assertEquals(listOf("a", "b"), removeLog)
    }

    @Test
    fun testMaxTime() = runBlocking {
        val removeLog = arrayListOf<String>()
        var time = 0L
        val cache = InmemoryCache<String, String>()
                .withTtl(maxMilliseconds = 10, onRemove = { removeLog += it }, clock = Clock { time.toDouble() })
                .withBuilder { it }

        time++; cache.get("a")
        time++; cache.get("b")
        time++; cache.get("c")
        assertEquals(listOf<String>(), removeLog)
        time += 10
        cache.get("d")
        assertEquals(listOf("a", "b", "c"), removeLog)
    }
}