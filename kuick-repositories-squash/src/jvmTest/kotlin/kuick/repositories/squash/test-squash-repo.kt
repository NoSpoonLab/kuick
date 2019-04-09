package kuick.repositories.squash

import kotlinx.coroutines.*
import kuick.db.*
import kuick.di.*
import kuick.models.*
import kuick.repositories.*
import kuick.repositories.annotations.*
import kuick.repositories.squash.orm.*
import org.jetbrains.squash.connection.*
import org.jetbrains.squash.dialects.h2.*
import org.junit.*

data class UserId(override val id: String) : Id

data class User(val userId: UserId,
                val firstName: String,
                val lastName: String,
                val ageOfUser: Int,
                val married: Boolean = false
)

fun main(args: Array<String>) = runBlocking {

    val db = H2Connection.createMemoryConnection()
    val injector = Guice { bindPerCoroutineJob() }
    val transactions = DomainTransactionServiceSquash(db, injector.get())
    db.monitor.before { println(it) }

    val repo = ModelRepositorySquash(User::class, User::userId)

    transactions {
        println("\nINIT ---------------------")
        repo.init()
    }

    transactions {
        println("\n---------------------")
        repo.insert(User(UserId("1"), "Mike", "Ballesteros", 44, true))
        repo.insert(User(UserId("2"), "Cristina", "García", 41, true))
        repo.insert(User(UserId("3"), "Jorge", "Ballesteros", 12))
        repo.insert(User(UserId("4"), "Marcos", "Ballesteros", 8))

        println("\n---------------------")
        val mike = repo.findById(UserId("1"))
        println("MIKE: ${mike}")

        suspend fun printQuery(query: ModelQuery<User>) {
            println("\n---------------------")
            val results = repo.findBy(query)
            results.forEach { println(" - ${it}") }
        }

        printQuery(User::lastName eq "Ballesteros")
        printQuery(User::ageOfUser gt 12)
        printQuery(User::ageOfUser gte 12)
        printQuery(User::married eq true)
    }
}
