package kuick.api.rpc

import com.google.inject.Injector
import io.ktor.application.call
import io.ktor.http.ContentType
import io.ktor.request.receiveText
import io.ktor.response.respondText
import io.ktor.routing.post
import io.ktor.util.AttributeKey
import kuick.api.buildArgsFromArray
import kuick.json.Json.gson
import kuick.ktor.KuickRouting
import kotlin.reflect.KFunction
import kotlin.reflect.full.callSuspend
import kotlin.reflect.full.memberFunctions


// TODO in gokoan rpc handles also downloading and uploading files (see: gokoan/backend/server/src/jvmMain/kotlin/koan/controllers/rpc-controller.kt)
// TODO discuss: should provide similar functionality here or make solution flexible enough so that user can provide it
data class RpcRouting(
        val kuickRouting: KuickRouting,
        val api: Any,
        val injector: Injector
) {
    fun registerAll() {
        api.visitRPC { srvName, method ->
            val path = "/rpc/$srvName/${method.name}"
            println("RPC: $path -> $method") // logging
            kuickRouting.routing.post(path) {
                val args = buildArgsFromArray(
                        method,
                        gson.toJsonTree(call.receiveText()),
                        call.attributes.allKeys.map { it.name to call.attributes[it as AttributeKey<Any>] }.toMap()
                )
                val result = method.callSuspend(api, *args.toTypedArray())


                call.respondText(gson.toJson(result), ContentType.Application.Json)
            }
        }
    }

    private fun Any.visitRPC(opAction: (String, KFunction<*>) -> Unit) {
        //TODO previous version of visitRPC did iterate over interfaces, i'm not sure if it's necessary
        val srvName = javaClass.simpleName
        javaClass.kotlin.memberFunctions.forEach { function ->
            try {
                opAction(srvName, function)
            } catch (ieme: Throwable) {
                println("WARN: invalid public method in controller: $function")
                ieme.printStackTrace()
            }
        }
    }
}


