package kuick.caching

import kotlinx.coroutines.*
import java.io.*
import kotlin.test.*

class DiskCacheTest {
    @Test
    fun test() {
        runBlocking {
            val dir = File(System.getProperty("java.io.tmpdir"))
            val key = "test"
            val instance = Demo(10)
            val cache = DiskCache("demo", dir).typeWithJsonNullable<Demo>()
            cache.invalidate(key)
            val test1 = cache.get(key) { instance }
            val test2 = cache.get(key) { instance }
            assertNotSame(instance, test2, "typeWithJsonNullable serialize always")
            assertNotSame(test1, test2)
            assertEquals(test1, test2)
        }
    }
}

private data class Demo(val a: Int)
