package kuick.client.repositories

import kotlinx.coroutines.*
import kuick.client.db.*
import kuick.client.jdbc.*
import kuick.repositories.*
import java.util.*
import kotlin.test.*

class DbModelRepositoryTest {
    @Test
    fun test() {
        data class Demo(val id: String, val v: Int)

        runBlocking {
            DbClientPool(1) { JdbcDriver.connectMemoryH2() }.use { pool ->
                withContext(pool) {
                    val repo = DbModelRepository(Demo::class, Demo::id)
                    repo.init()
                    repo.insert(Demo("hello", 10))
                    repo.insert(Demo("hello", 20))
                    repo.insert(Demo("world", 30))
                    assertEquals(3, repo.getAll().size)
                    assertEquals(2, repo.findBy(Demo::id eq "hello").size)
                    assertEquals(1, repo.findBy(Demo::id eq "hello", limit = 1).size)
                    repo.update(Demo("world", 30).copy(v = 40))
                    assertEquals(Demo("world", 40), repo.findOneBy(Demo::id eq "world"))
                }
            }
        }
    }

    @Test
    fun testDate() {
        data class Demo(val date: Date)

        runBlocking {
            DbClientPool(1) { JdbcDriver.connectMemoryH2() }.use { pool ->
                withContext(pool) {
                    val repo = DbModelRepository(Demo::class, Demo::date)
                    repo.init()
                    repo.insert(Demo(Date(2017, 1, 1)))
                    repo.insert(Demo(Date(2018, 1, 1)))
                    repo.insert(Demo(Date(2019, 1, 1)))
                    assertEquals(2, repo.findBy(Demo::date gte Date(2018, 1, 1)).size)
                }
            }
        }
    }

    @Test
    fun testUpdateWithIncrement() {
        data class Entity(val id: String, val counter: Int)

        runBlocking {
            DbClientPool(1) { JdbcDriver.connectMemoryH2() }.use { pool ->
                withContext(pool) {
                    val repo = DbModelRepository(Entity::class, Entity::id)
                    repo.init()
                    val id = "test"
                    val noId = "NO ID"
                    repo.insert(Entity(id, 100))
                    assertEquals(1, repo.update(incr = mapOf(Entity::counter to +1), where = Entity::id eq id))
                    assertEquals(0, repo.update(incr = mapOf(Entity::counter to +1), where = Entity::id eq noId))
                    assertEquals(101, repo.findById(id)?.counter)
                }
            }
        }
    }
}
