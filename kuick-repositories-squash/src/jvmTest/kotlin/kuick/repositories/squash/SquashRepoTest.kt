package kuick.repositories.squash

import kotlinx.coroutines.*
import kuick.repositories.annotations.*
import org.h2.jdbc.*
import org.jetbrains.squash.dialects.h2.*
import org.junit.Test
import kotlin.test.*

class SquashRepoTest {
    data class User(@MaxLength(4) val a: String, @MaxLength(2) val b: String)

    @Test
    fun `maxLength is taken into account`(): Unit = runBlocking {
        val db = H2Connection.createMemoryConnection()
        val repo = ModelRepositorySquash(User::class, User::a)
        db.runInTransaction {
            repo.init()
        }
        db.runInTransaction {
            repo.insert(User("1234", "12"))
        }
        assertFailsWith<JdbcSQLException> {
            db.runInTransaction {
                repo.insert(User("12345", "123"))
            }
        }
        Unit
    }
}