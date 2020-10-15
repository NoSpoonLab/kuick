package kuick.repositories.jasync

import kotlinx.coroutines.runBlocking
import kuick.repositories.eq

fun main() {

    val pool = JasyncPool(
        "host",
        5432,
        "dbname",
        "user",
        "pwd",
        debug = true
    )

    val leadRepo = ModelRepositoryJasync(Lead::class, "lead", Lead::leadId, pool)
    val userRepo = ModelRepositoryJasync(User::class, "\"User\"", User::userId, pool)



    runBlocking {
        val l1 = leadRepo.findBy(Lead::email eq "maballesteros@gmail.com")
        println(l1)
        val user = userRepo.findById("google-113220514328457344729")
        val user2 = userRepo.findBy(User::email eq "maballesteros@gmail.com")
        println(user)
        println(user2)

    }
}

data class User(
    val userId: String,
    val createdAt: Long,
    val name: String,
    val email: String
)

data class Lead(
    val leadId: String,
    val createdAt: Long,
    val updatedAt: Long,
    val email: String
)
