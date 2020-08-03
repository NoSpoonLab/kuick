package kuick.samples.rpc.users


interface UsersApi {

    suspend fun testUser(): User

    suspend fun findMyUser(): User

    suspend fun findUserById(userId: String): User?

    suspend fun findUserByEmail(email: String): User?

}
