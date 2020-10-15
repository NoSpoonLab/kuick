package kuick.api.rpc

import kotlinx.coroutines.withContext
import kuick.logging.LogLevel
import kuick.logging.Logger
import kuick.utils.randomUUID
import java.text.SimpleDateFormat
import java.util.*
import kotlin.coroutines.AbstractCoroutineContextElement
import kotlin.coroutines.CoroutineContext
import kotlin.coroutines.coroutineContext

const val RPC_CONTEXT_HEADER = "Kuick-RPC"

data class RpcContext(
    val requestId: String,
    val at: Long,
    val token: String,
    val userId: String?
) {
    companion object {
        val dateFormat = SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS")
        fun random() = RpcContext(randomUUID(), System.currentTimeMillis(), "<TOKEN>", null)
    }
}

// coroutine context element that keeps RpcContext
class RpcCoroutineContext(val rpcContext: RpcContext) : AbstractCoroutineContextElement(
    Key
) {
    companion object Key : CoroutineContext.Key<RpcCoroutineContext>
}

suspend fun <T> withRpcContext(rpcContext: RpcContext, block: suspend () -> T): T {
    return withContext(RpcCoroutineContext(rpcContext)) {
        block()
    }
}


suspend fun rpcContext(): RpcContext? = coroutineContext[RpcCoroutineContext]?.rpcContext

inline suspend fun Logger.traceRpc(message: () -> String) {
    rpcContext()
        ?.let {
            log(LogLevel.TRACE,  "[${RpcContext.dateFormat.format(Date(it.at))}] [${it.requestId}] ${message()}", null)
        }
}
