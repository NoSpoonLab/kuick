package kuick.caching.db

import kotlinx.coroutines.*
import kuick.caching.*
import kuick.util.*
import kotlin.test.*

class DbCacheInvalidationTest {
    data class Demo(val a: Int)

    @Test
    fun test() {
        //runBlocking {
        //    DbCacheInvalidation.get { invalidation ->
        //        DiskCache<Demo>("MyCacheName").invalidatedBy("MyCacheName", invalidation).use { cache ->
        //            cache.invalidate("test")
        //        }
        //    }
        //}
    }
}
