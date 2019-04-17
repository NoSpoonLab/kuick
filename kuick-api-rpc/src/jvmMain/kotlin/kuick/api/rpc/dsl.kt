package kuick.api.rpc

import com.google.inject.Injector
import kuick.ktor.KuickRouting


inline fun <reified T> KuickRouting.rpcRouting(injector: Injector, configuration: RpcRouting.() -> Unit): RpcRouting {
    val api = injector.getInstance(T::class.java)!!
    return RpcRouting(this, api, injector)
            .apply(configuration)
            .also { it.registerAll() }
}

