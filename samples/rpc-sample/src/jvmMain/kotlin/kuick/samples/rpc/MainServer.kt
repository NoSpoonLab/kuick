package kuick.samples.rpc

import kotlinx.coroutines.runBlocking
import kuick.api.rpc.RpcServer
import kuick.repositories.jasync.JasyncPool
import kuick.samples.rpc.users.UsersApi
import kuick.samples.rpc.users.UsersApiImpl
import kuick.samples.rpc.users.UsersRepository

fun main() = runBlocking {

    // Service
    val usersApi: UsersApi = UsersApiImpl()

    RpcServer("SampleRPC", 8080, listOf(
        usersApi
    )).start(true, true)

}
