package kuick.api.rpc

import com.google.inject.Injector
import io.ktor.application.call
import io.ktor.http.ContentType
import io.ktor.request.receiveText
import io.ktor.response.respondText
import io.ktor.routing.Route
import io.ktor.routing.post
import io.ktor.util.AttributeKey
import kuick.api.buildArgsFromArray
import kuick.api.getAsTree
import kuick.api.parameters.include.IncludeParam
import kuick.api.parameters.include.includeRelatedResources
import kuick.api.parameters.preserve.FieldsParam
import kuick.api.parameters.preserve.preserveFields
import kuick.json.Json.gson
import kuick.orm.clazz
import kotlin.reflect.KFunction
import kotlin.reflect.KProperty
import kotlin.reflect.full.callSuspend
import kotlin.reflect.full.isSubtypeOf
import kotlin.reflect.full.memberFunctions
import kotlin.reflect.full.starProjectedType


data class RpcRouting(
        val parent: Route,
        val api: Any,
        val injector: Injector,
        val config: Configuration = Configuration()
) {
    fun registerAll() {
        api.visitRPC { srvName, method ->
            val path = "/rpc/$srvName/${method.name}"
            println("RPC: $path -> $method") // logging
            parent.post(path) {
                val args = buildArgsFromArray(
                        method,
                        gson.toJsonTree(call.receiveText()),
                        call.attributes.allKeys.map { it.name to call.attributes[it as AttributeKey<Any>] }.toMap()
                )

                val responseClass = method.returnType.run {
                    return@run if (isSubtypeOf(List::class.starProjectedType)) {
                        this.arguments[0].type!!.clazz
                    } else {
                        this.clazz
                    }
                }.java

                val result = method.callSuspend(api, *args.toTypedArray())
                val jsonResult = gson.toJsonTree(result)

                val queryParameters = call.request.queryParameters

                //TODO handle case when we include smth just to cut it out when filtering
                if (config.includeParameterConfiguration != null && "\$include" in queryParameters) {
                    val configuration = config.includeParameterConfiguration!!
                    val includeParam = IncludeParam.create(queryParameters.getAsTree("\$include"), responseClass, configuration)
                    jsonResult.includeRelatedResources(includeParam, configuration)
                }

                if (config.withFieldsParameter && "\$fields" in queryParameters) {
                    val fieldsParam = FieldsParam.create(queryParameters.getAsTree("\$fields"), responseClass)
                    jsonResult.preserveFields(fieldsParam)
                }

                call.respondText(jsonResult.toString(), ContentType.Application.Json)
            }
        }
    }

    fun Any.visitRPC(opAction: (String, KFunction<*>) -> Unit) {
        val srvName = javaClass.simpleName
        javaClass.kotlin.memberFunctions.forEach { function ->
            try {
                opAction(srvName, function)
            } catch (exception: Throwable) {
                println("WARN: invalid public method in controller: $function")
                exception.printStackTrace()
            }
        }
    }

    class Configuration {
        var withFieldsParameter: Boolean = false
            private set
        var includeParameterConfiguration: Map<String, suspend (id: String) -> Any>? = null
            private set

        fun withFieldsParameter() {
            withFieldsParameter = true
        }

        fun withIncludeParameter(vararg configuration: Pair<KProperty<Any?>, suspend (id: String) -> Any>) {
            includeParameterConfiguration = configuration
                    .map { it.first.name to it.second }.toMap()
        }
    }
}


