package kuick.api.rpc

import kotlin.reflect.KClass

interface RpcClient {

    suspend fun <T: Any> call(serviceBaseUrl: String, srvName: String, opName: String, params: List<Any?>, returnType: KClass<T>): T
}

