package kuick.api.rpc

import io.ktor.client.HttpClient
import io.ktor.client.engine.apache.Apache
import io.ktor.client.features.json.JsonFeature
import io.ktor.client.request.header
import io.ktor.client.request.post
import io.ktor.client.request.url
import kuick.json.Json
import kuick.logging.Logger
import kotlin.reflect.KClass


data class RpcClientJvm(
    val client: HttpClient =
        HttpClient(Apache) {
            install(JsonFeature) {
                //serializer = GsonSerializer {}
            }
        }
): RpcClient {

    val logger = Logger(RpcClientJvm::class.simpleName!!)

    override suspend fun <T : Any> call(serviceBaseUrl: String, srvName: String, opName: String, params: List<Any?>, returnType: KClass<T>): T {
try {


    val endpoint = "$serviceBaseUrl/$srvName/$opName"

    val rpcContext: RpcContext = rpcContext() ?: RpcContext.random()

    val bearerSecret = "<PENDING>"

    val beginAt = System.currentTimeMillis()
    logger.traceRpc { "Calling $endpoint with context $rpcContext" }
    logger.traceRpc { "  ==> $params" }
    val responseJson = client.post<String> {
        url(endpoint)
        headers.clear()
        header("Content-Type", "application/json")
        header("Authorization", "Bearer $bearerSecret")
        header(RPC_CONTEXT_HEADER, Json.toJson(rpcContext))
        body = params
    }
    val endAt = System.currentTimeMillis()
    val lapse = endAt - beginAt
    logger.traceRpc { "  <== $responseJson as ${returnType} ($lapse ms)" }

    return Json.fromJson(responseJson, returnType)
} catch (t: Throwable) {
    t.printStackTrace()
    throw t
}
    }


    /*
    fun <T:Any> proxy(serviceBaseUrl: String, iface: KClass<T>): T = Proxy.newProxyInstance(
        Thread.currentThread().contextClassLoader,
        arrayOf<Class<*>>(iface.java),
        { _, method, methodArgs -> call(serviceBaseUrl, method, methodArgs) }
    ) as T

    fun call(serviceBaseUrl: String, method: Method, params: Array<Any>): Any = runBlocking {
        val kfun = method.kotlinFunction!!
        val srvName = method.declaringClass.simpleName
        val opName = method.name

        val opArgs = params.dropLast(1)
        val continuation = params.last() as Continuation<Any?>

        val rpcCoroutineContext = continuation.context[RpcCoroutineContext]
        val rpcContext: RpcContext = rpcCoroutineContext?.rpcContext ?: RpcContext.random()
        withRpcContext(rpcContext) {
            val endpoint = "$serviceBaseUrl/$srvName/$opName"

            val rpcContext: RpcContext = rpcContext() ?: RpcContext.random()

            val bearerSecret = "<PENDING>"

            val beginAt = System.currentTimeMillis()
            logger.traceRpc { "Calling $endpoint with context $rpcContext" }
            logger.traceRpc { "  ==> $params" }
            val responseJson = client.post<String> {
                url(endpoint)
                headers.clear()
                header("Content-Type", "application/json")
                header("Authorization", "Bearer $bearerSecret")
                header(RPC_CONTEXT_HEADER, Json.toJson(rpcContext))
                body = params
            }
            val endAt = System.currentTimeMillis()
            val lapse = endAt - beginAt
            logger.traceRpc { "  <== $responseJson as ${kfun.returnType} ($lapse ms)" }

            Json.fromJson(responseJson, kfun.returnType) as Any
        }
    }
    */
 }
