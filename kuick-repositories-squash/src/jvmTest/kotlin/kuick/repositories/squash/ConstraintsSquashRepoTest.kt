package kuick.repositories.squash

import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withContext
import kuick.db.DomainTransactionContext
import kuick.db.domainTransaction
import kuick.repositories.annotations.MaxLength
import org.h2.jdbc.JdbcSQLException
import org.junit.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertTrue

class ConstraintsSquashRepoTest: AbstractITTest() {
    data class User(@MaxLength(4) val a: String, @MaxLength(2) val b: String)

    @Test
    fun `maxLength is taken into account`(): Unit = testInTransaction {
        val repo = ModelRepositorySquash(User::class, User::a)
        repo.init()
        repo.insert(User("1234", "12"))

        val tr = domainTransaction()
        assertTrue(
            assertFailsWith<JdbcSQLException> {
                runBlocking {
                    withContext(DomainTransactionContext(tr)) {
                        repo.insert(User("12345", "123"))
                    }
                }
            }.message!!.contains(
                    "\"A VARCHAR(4) NOT NULL\": \"'12345' (5)\"; SQL statement:\n" +
                            "INSERT INTO \"User\" (a, b) VALUES (?, ?) [22001-197]"
            )
        )
    }
}
