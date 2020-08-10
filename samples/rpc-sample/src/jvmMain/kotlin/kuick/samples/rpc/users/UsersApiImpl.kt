package kuick.samples.rpc.users

import kuick.api.rpc.rpcContext

class UsersApiImpl(): UsersApi {

    override suspend fun testUser(): User =
        User("testUserId", "Test User", "user@test.com", 0L)

    override suspend fun findMyUser(): User =
        findUserById(rpcContext()!!.userId!!)!!

    override suspend fun findUserById(userId: String): User? =
        User(userId, "User $userId", "$userId@test.com", 0L)

    override suspend fun findUserByEmail(email: String): User? =
        User("userId-email", "User $email", email, 0L)
}
