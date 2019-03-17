package kuick.repositories.squash

import kuick.models.Id
import kuick.repositories.eq
import kuick.repositories.gt
import kuick.repositories.gte
import kotlin.test.Test
import kotlin.test.assertEquals


class BasicOpsSquashRepoTest: AbstractITTest() {

    data class UserId(override val id: String): Id

    data class User(val userId: UserId,
                    val firstName: String,
                    val lastName: String,
                    val ageOfUser: Int,
                    val married: Boolean,
                    val createdAt: Long,
                    val updatedAt: Long? = null
    )


    @Test
    fun `test basic insert and search ops`() = testInTransaction {
        val repo = ModelRepositorySquash(User::class, User::userId)
        repo.init()

        val mike = user("1", "Mike Ballesteros", 44, true)

        val users = listOf(
                mike,
                user("2", "Cristina García", 41, true),
                user("3", "Jorge Ballesteros", 12, false),
                user("4", "Marcos Ballesteros", 8, false),
                user("5", "Daniel Ballesteros", 3, false)
        )
        users.forEach { repo.insert(it) }


        assertEquals(mike, repo.findById(mike.userId))

        assertEquals(4, repo.findBy(User::lastName eq "Ballesteros").size)
        assertEquals(2, repo.findBy(User::ageOfUser gt 12).size)
        assertEquals(3, repo.findBy(User::ageOfUser gte 12).size)
        assertEquals(2, repo.findBy(User::married eq true).size)

    }


    @Test
    fun `test basic update ops`() = testInTransaction {
        val repo = ModelRepositorySquash(User::class, User::userId)
        repo.init()

        val mike = user("1", "Mike Ballesteros", 44, true)
        repo.insert(mike)
        assertEquals(mike, repo.findById(mike.userId))

        val mike2 = mike.copy(updatedAt = System.currentTimeMillis())
        repo.update(mike2)
        assertEquals(mike2, repo.findById(mike.userId))
    }



    private fun user(id: String, fullName: String, age: Int, married: Boolean) =
            User(UserId(id),
                    fullName.substringBefore(' '),
                    fullName.substringAfter(' '),
                    age,
                    married,
                    System.currentTimeMillis(),
                    updatedAt = null)
}
