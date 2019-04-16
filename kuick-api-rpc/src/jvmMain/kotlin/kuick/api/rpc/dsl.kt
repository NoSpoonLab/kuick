package kuick.api.rpc

import com.google.inject.Injector
import io.ktor.application.call
import io.ktor.http.ContentType
import io.ktor.request.receiveText
import io.ktor.response.respondText
import io.ktor.routing.post
import kuick.api.buildArgs
import kuick.api.invokeHandler
import kuick.json.Json.gson
import kuick.ktor.KuickRouting


inline fun <reified T> KuickRouting.rpcRouting(injector: Injector) {
    val api = injector.getInstance(T::class.java)!!
    api.visitRPC { srvName, method ->
        val path = "/rpc/$srvName/${method.name}"
        println("RPC: $path -> $method") // logging
        routing.post(path) {
            val args = buildArgs(
                    method,
                    gson.toJsonTree(call.receiveText()),
                    emptyMap() // TODO
            )
            val result = invokeHandler(api, method, args)
            call.respondText(gson.toJson(result), ContentType.Application.Json)
        }
    }
}

