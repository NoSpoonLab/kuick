package kuick.caching.db

import kotlinx.coroutines.*
import kuick.caching.*
import kuick.client.db.*
import kuick.client.jdbc.*
import kuick.util.*
import kotlin.test.*

class DbCacheInvalidationTest {
    data class Demo(val value: Int)

    @Test
    fun test() {
        runBlocking {
            //JdbcDriver.connectMemoryH2().log().use { connection ->
            JdbcDriver.connectMemoryH2().use { connection ->
                withContext(DbConnectionProvider { connection }) {
                    DbCacheInvalidation.get(delay = 100L) { invalidation ->
                        //invalidation.invalidateAll("MyCacheName")
                        val invalidated = CompletableDeferred<Unit>()
                        val invalidationLog = arrayListOf<String>()
                        InmemoryCache<String, Demo>().interceptInvalidation { invalidationLog += it }.interceptInvalidation { invalidated.complete(Unit) }.withInvalidation(invalidation).use { cache ->
                            val instance1 = cache.get("test") { Demo(1) }
                            val instance2 = cache.get("test") { Demo(2) }
                            assertEquals(1, instance1.value)
                            assertEquals(1, instance2.value) // Cached
                            cache.invalidate("test")
                            invalidated.await()
                            assertEquals(listOf("test"), invalidationLog)
                            val instance3 = cache.get("test") { Demo(3) }
                            assertEquals(3, instance3.value)
                        }
                    }
                }
            }
        }
    }
}
