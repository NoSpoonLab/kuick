package kuick.samples.rpc.users

import kuick.api.rpc.rpcContext
import kuick.repositories.eq

class UsersApiImpl(
    val usersRepo: UsersRepository
): UsersApi {

    override suspend fun testUser(): User =
        User("testUserId", "Test User", "user@test.com", 0L)

    override suspend fun findMyUser(): User =
        findUserById(rpcContext()!!.userId!!)!!

    override suspend fun findUserById(userId: String): User? =
        usersRepo.findById(userId)

    override suspend fun findUserByEmail(email: String): User? =
        usersRepo.findOneBy(User::email eq email)
}
