package kuick.samples.rpc.users

import kuick.api.rpc.*

class RemoteUsersApi(
    serviceBaseUrl: String,
    rpcClient: RpcClient
): AbstractRemoteApi(serviceBaseUrl, UsersApi::class, rpcClient), UsersApi {

    override suspend fun testUser(): User = call("testUser")

    override suspend fun findMyUser(): User = call ("findMyUser")

    override suspend fun findUserById(userId: String): User? = call ("findUserById", userId)

    override suspend fun findUserByEmail(email: String): User? = call("findUserByEmail", email)

}
