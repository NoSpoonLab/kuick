package kuick.caching

import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withContext
import kuick.client.db.DbConnectionProvider
import kuick.client.jdbc.JdbcDriver
import kuick.client.repositories.DbModelRepository
import kuick.utils.use
import kotlin.test.Test
import kotlin.test.assertEquals

class DbCacheInvalidationTest {
    data class Demo(val value: Int)

    @Test
    fun test() {
        runBlocking {
            //JdbcDriver.connectMemoryH2().log().use { connection ->
            JdbcDriver.connectMemoryH2().use { connection ->
                withContext(DbConnectionProvider { connection }) {
                    val cc = coroutineContext
                    DbCacheInvalidation.get(
                        delay = 100L,
                        repo = DbModelRepository(DbCacheInvalidation.CacheInvalidationEntry::cacheNameKey),
                        setCoroutineContext = { withContext(cc) { it() } }) { invalidation ->
                        //invalidation.invalidateAll("MyCacheName")
                        val invalidated = CompletableDeferred<Unit>()
                        val invalidationLog = arrayListOf<String>()
                        InmemoryCache<String, Demo>().interceptInvalidation { invalidationLog += it }.interceptInvalidation { invalidated.complete(Unit) }
                            .withInvalidation(invalidation).use { cache ->
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
