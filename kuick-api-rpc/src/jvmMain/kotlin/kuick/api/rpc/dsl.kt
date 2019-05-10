package kuick.api.rpc

import com.google.inject.Injector
import io.ktor.routing.Route


inline fun <reified T> Route.rpcRoute(injector: Injector, configuration: RpcRouting.() -> Unit = {}): RpcRouting {
    val api = injector.getInstance(T::class.java)!!
    return RpcRouting(this, api, injector)
            .apply(configuration)
            .also { it.registerAll() }
}

