package kuick.repositories.squash

import kotlinx.coroutines.*
import kuick.db.*
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


fun DatabaseConnection.runInTransaction(actions: suspend () -> Unit) = transaction {
    val tr = DomainTransactionSquash(this)
    runBlocking {
        withContext(DomainTransactionContext(tr)) {
            actions()
        }
    }
}

fun main(args: Array<String>) {

    val db = H2Connection.createMemoryConnection()
    db.monitor.before { println(it) }

    val repo = ModelRepositorySquash(User::class, User::userId)

    db.runInTransaction {
        println("\nINIT ---------------------")
        repo.init()
    }

    db.runInTransaction {
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
