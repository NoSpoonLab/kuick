package kuick.repositories.squash

import kotlinx.coroutines.*
import kuick.db.*
import kuick.di.*
import kuick.repositories.*
import kuick.repositories.annotations.*
import org.h2.jdbc.*
import org.jetbrains.squash.dialects.h2.*
import org.junit.Test
import java.time.*
import kotlin.reflect.*
import kotlin.test.*

class SquashRepoTest {
    data class User(@MaxLength(4) val a: String, @MaxLength(2) val b: String)
    data class Points(val id: String, val points: Long)

    @Test
    fun `maxLength is taken into account`(): Unit = runBlocking {
        val db = H2Connection.createMemoryConnection()
        val repo = ModelRepositorySquash(User::class, User::a)
        val injector = Guice {
            bindDatabaseSquash(db)
        }
        withInjectorContext(injector) {
            repo.init()
            repo.insert(User("1234", "12"))
            val context = coroutineContext
            assertTrue(
                    assertFailsWith<JdbcSQLException> {
                        runBlocking(context) {
                            repo.insert(User("12345", "123"))
                        }
                    }.message!!.contains(
                            "\"A VARCHAR(4) NOT NULL\": \"'12345' (5)\"; SQL statement:\n" +
                                    "INSERT INTO \"User\" (a, b) VALUES (?, ?) [22001-197]"
                    )
            )
        }
    }

    @Test
    fun incrementValues(): Unit = runBlocking {
        val db = H2Connection.createMemoryConnection()
        val repo = ModelRepositorySquash(Points::class, Points::id)
        val injector = Guice {
            bindDatabaseSquash(db)
        }
        withInjectorContext(injector) {
            repo.init()
            val id = "1234"
            val nonExistantId = "777"
            repo.insert(Points(id, 100L))
            assertEquals(1, repo.update(incr = mapOf(Points::points to +1), where = Points::id eq id))
            assertEquals(101L, repo.findById(id)?.points)
            assertEquals(0, repo.update(incr = mapOf(Points::points to +1), where = Points::id eq nonExistantId))
            assertEquals(101L, repo.findById(id)?.points)
        }
    }

    data class NullTest(
            val a: String,
            val b: LocalDate? = null
    )

    @Test
    fun `null is taken into account`(): Unit = runBlocking {
        val db = H2Connection.createMemoryConnection()
        val repo = ModelRepositorySquash(NullTest::class, NullTest::a)
        val injector = Guice { bindDatabaseSquash(db) }
        withInjectorContext(injector) {
            repo.init()
            repo.insert(NullTest("test", null))
            repo.insert(NullTest("test2", null))
            repo.insert(NullTest("test3", LocalDate.of(2019, Month.FEBRUARY, 1)))
            Unit
        }
    }

    data class PrimaryUnique(
            //@Primary
            val userId: String
    )

    @Test
    fun `primary column is unique`(): Unit = runBlocking {
        val db = H2Connection.createMemoryConnection()
        val repo = ModelRepositorySquash(PrimaryUnique::class, PrimaryUnique::userId)
        val injector = Guice { bindDatabaseSquash(db) }
        withInjectorContext(injector) {
            repo.init()
            repo.insert(PrimaryUnique("a"))
            repo.insert(PrimaryUnique("a"))
            Unit
        }
    }

    data class UserEmail(
            val userId: String,
            @Unique @MaxLength(64) val email: String
    )

    @Test
    fun `unique is taken into account`(): Unit = runBlocking {
        val db = H2Connection.createMemoryConnection()
        val repo = ModelRepositorySquash(UserEmail::class, UserEmail::userId)
        val injector = Guice { bindDatabaseSquash(db) }
        withInjectorContext(injector) {
            repo.init()
            repo.insert(UserEmail("a", "test"))
            assertFailsWith<JdbcSQLException> {
                repo.insert(UserEmail("b", "test"))
            }
            Unit
        }
    }

    @Test
    fun `queries can limit and order`(): Unit = runBlocking {
        data class Demo(val name: String, val v: Int)

        val db = H2Connection.createMemoryConnection()
        val repo = ModelRepositorySquash(Demo::class, Demo::name)
        val injector = Guice { bindDatabaseSquash(db) }
        withInjectorContext(injector) {
            repo.init()
            repo.insert(Demo("a", 1))
            repo.insert(Demo("b", 2))
            repo.insert(Demo("c", 2))
            repo.insert(Demo("d", 4))
            repo.insert(Demo("e", 5))
            assertEquals(
                    listOf(Demo("d", 4), Demo("b", 2)),
                    repo.findBy(Demo::v gt 1, skip = 1, limit = 2, orderBy = Demo::v.desc() + Demo::name.asc())
            )
            Unit
        }
    }
}

inline fun <reified T : kotlin.Throwable> assertFailsWith(block: () -> kotlin.Unit): T {
    val clazz = T::class
    val e = try {
        block()
        null
    } catch (e: Throwable) {
        e
    } ?: fail("Expected to throw $clazz")
    if (!clazz.isInstance(e)) error("Expected to throw $clazz, but thrown $e")
    return e as T
}
