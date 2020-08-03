package kuick.samples.rpc

import kotlinx.coroutines.runBlocking
import kuick.api.rpc.RpcServer
import kuick.repositories.jasync.JasyncPool
import kuick.samples.rpc.users.UsersApi
import kuick.samples.rpc.users.UsersApiImpl
import kuick.samples.rpc.users.UsersRepository

fun main() = runBlocking {

    // DB pool
    val pool = JasyncPool(
        "host",
        5432,
        "db",
        "user",
        "pwd",
        maxActiveConnections = 10,
        debug = false
    )

    // Repos
    val usersRepository = UsersRepository(pool)

    // Service
    val usersApi: UsersApi = UsersApiImpl(usersRepository)

    RpcServer("SampleRPC", 8080, listOf(
        usersApi
    )).start(true)

}
