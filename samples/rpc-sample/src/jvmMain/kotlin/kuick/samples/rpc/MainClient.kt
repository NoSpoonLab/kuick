package kuick.samples.rpc

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.runBlocking
import kuick.api.rpc.RpcClient
import kuick.api.rpc.RpcClientJvm
import kuick.api.rpc.RpcContext
import kuick.api.rpc.withRpcContext
import kuick.samples.rpc.users.RemoteUsersApi
import kuick.utils.randomUUID

fun main() = runBlocking {
    launchClientPerformanceTest()
}

suspend fun CoroutineScope.launchClientPerformanceTest() {
    val rpcClient: RpcClient = RpcClientJvm()
    val perfUsersApi = RemoteUsersApi("http://localhost:8080", rpcClient)

    val mike = perfUsersApi.findUserByEmail("lvalorcubero@gmail.com")!!
    println(mike)

    val testApi = perfUsersApi//remoteUsersApi
    val loops = 2000
    val t0 = System.currentTimeMillis()
    val jobs = (1..loops).map {
        async {

            // REMOTE BUT NOT DB
            //testApi.testUser()

            // REMOTE WITH DB
            //testApi.findUserById(mike.userId)

            // REMOTE WITH DB AND CONTEXT
            withRpcContext(RpcContext(randomUUID(), System.currentTimeMillis(),"***", mike.userId)) {
                testApi.findMyUser()
            }
        }
    }
    val t1 = System.currentTimeMillis()
    jobs.awaitAll()

    val t2 = System.currentTimeMillis()
    val launchTime = t1 - t0
    val completeTime = t2 - t0
    println("\n\n")
    println("*********")
    println("LAUNCH: $launchTime ms (${(loops * 1000) / launchTime} requests/sec)")
    println("TOTAL TIME: $completeTime ms (${completeTime / loops} ms/loop avg)")
    println("*********")
}
