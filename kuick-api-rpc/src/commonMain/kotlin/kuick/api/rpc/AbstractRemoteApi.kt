package kuick.api.rpc

import kotlin.reflect.KClass

abstract class AbstractRemoteApi(
    val serviceBaseUrl: String,
    val serviceIface: KClass<*>,
    val rpcClient: RpcClient
) {
    protected inline suspend fun <reified T:Any> call(op: String, vararg params: Any?): T =
        rpcClient.call<T>(serviceBaseUrl, serviceIface.simpleName!!, op, params.toList(), T::class)
}
