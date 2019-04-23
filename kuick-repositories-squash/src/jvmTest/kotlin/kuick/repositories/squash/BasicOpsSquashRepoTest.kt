package kuick.repositories.squash

import kuick.models.Id
import kuick.repositories.eq
import kuick.repositories.gt
import kuick.repositories.gte
import kuick.repositories.within
import java.util.*
import kotlin.test.Test
import kotlin.test.assertEquals

// TODO This should be a conformance test: should be a generic test that takes
//  a repository implementation and perform every testable operation, so it can
//  be used to test not only this squash implementation but every implementation
class BasicOpsSquashRepoTest: AbstractITTest() {

    data class UserId(override val id: String): Id

    data class User(val userId: UserId,
                    val firstName: String,
                    val lastName: String,
                    val ageOfUser: Int,
                    val married: Boolean,
                    val createdAt: Date,
                    val updatedAt: Date? = null
    )

    data class ResultDetails(val demo: Double = 1.0)
    data class UserResult(val userId: UserId, val score: Double, val details: ResultDetails)


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

        assertEquals(4,
                repo.findBy(User::lastName within setOf("Ballesteros")).size,
                "Within should work on simple fields")

        assertEquals(2,
                repo.findBy(User::ageOfUser within setOf(0, 3, 8)).size,
                "Within should work OK with missing values (0 in this case)")

        assertEquals(2, repo.findBy(User::userId within
                setOf(UserId("1"), UserId("2"))).size,
                "Within should work with ID fields")

        assertEquals(0,
                repo.findBy(User::ageOfUser within setOf()).size,
                "Within should work OK empty sets")

    }


    @Test
    fun `test basic update ops`() = testInTransaction {
        val repo = ModelRepositorySquash(User::class, User::userId)
        repo.init()

        val mike = user("1", "Mike Ballesteros", 44, true)
        repo.insert(mike)
        assertEquals(mike, repo.findById(mike.userId))

        val mike2 = mike.copy(updatedAt = Date())
        repo.update(mike2)
        assertEquals(mike2, repo.findById(mike.userId))
    }

    @Test
    fun `test double`() = testInTransaction {
        val repo = ModelRepositorySquash(UserResult::class, UserResult::userId)
        repo.init()
        val result = UserResult(UserId("test"), 1.0, ResultDetails(2.0))
        repo.insert(result)
        assertEquals(listOf(result), repo.getAll())
    }


    private fun user(id: String, fullName: String, age: Int, married: Boolean) =
            User(UserId(id),
                    fullName.substringBefore(' '),
                    fullName.substringAfter(' '),
                    age,
                    married,
                    Date(),
                    updatedAt = null)
}
