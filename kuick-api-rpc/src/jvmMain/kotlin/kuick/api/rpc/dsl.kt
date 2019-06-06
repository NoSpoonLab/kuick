package kuick.api.rpc

import com.google.inject.Injector
import io.ktor.routing.Route


inline fun <reified T> Route.rpcRoute(injector: Injector, config: RpcRouting.Configuration.() -> Unit = {}): RpcRouting {
    val api = injector.getInstance(T::class.java)!!
    return RpcRouting(this, api, injector)
            .apply { config(this.config) }
            .also { it.registerAll() }
}

