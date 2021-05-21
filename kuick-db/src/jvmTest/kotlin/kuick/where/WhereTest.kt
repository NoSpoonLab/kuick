package kuick.where

import kotlinx.coroutines.*
import kuick.client.where.*
import kuick.repositories.memory.*
import java.util.concurrent.*
import kotlin.test.*

class WhereTest {
    data class Question(val id: String, val category: String, val question: String, val date: Long)

    // Equivalent to Ruby on Rails scopes
    val Where<Question>.lastWeek get() = this.where { (it::date ge System.currentTimeMillis() - TimeUnit.DAYS.toMillis(7L)) }
    fun Where<Question>.category(cat: String) = this.where { (it::category eq cat) }

    @Test
    fun test(): Unit = runBlocking {
        val repo = ModelRepositoryMemory(Question::class, Question::id)
        val cat1 = "cat1"
        val cat2 = "cat2"

        lateinit var q0: Question

        repo.insert(Question("0", cat1, "hello", System.currentTimeMillis() - 1000).also { q0 = it })
        repo.insert(Question("1", cat1, "hello", System.currentTimeMillis() - (TimeUnit.DAYS.toMillis(10L))))
        repo.insert(Question("2", cat2, "world", System.currentTimeMillis() - 1000))

        val cat1Rows = repo.where
            .category(cat1)

        assertEquals(2, cat1Rows.count())
        assertEquals(1, cat1Rows.lastWeek.count())
        assertEquals(listOf(q0), cat1Rows.lastWeek.find())
    }

}
