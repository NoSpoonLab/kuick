package kuick.caching

import kotlinx.coroutines.runBlocking
import kuick.bus.SyncBus
import kotlin.test.Test
import kotlin.test.assertEquals

class CachingTest {

    val bus = SyncBus()
    val cacheManager = CacheManager(bus)
    val repo: DemoUserRepository = DemoUserRepositoryImpl()
    val srv = DemoUserService(cacheManager, repo)
    val appId = "app1"


    @Test
    fun `simple cache and invalidation`() = runBlocking {

        val mike = srv.createUser(appId, "mike", "Mike", 43)
        assertEquals(listOf(mike), srv.getAllAppUsers(appId))

        val cris = srv.createUser(appId, "cris", "Cris", 40)
        assertEquals(listOf(mike, cris), srv.getAllAppUsers(appId))
    }


    @Test
    fun `1 derivated cache and invalidation`() = runBlocking {

        val mike = srv.createUser(appId, "mike", "Mike", 43)
        assertEquals(listOf(mike), srv.getAdultAppUsers(appId))

        val cris = srv.createUser(appId, "cris", "Cris", 40)
        assertEquals(listOf(mike, cris), srv.getAdultAppUsers(appId))

    }
}