package kuick.api.rpc

import com.google.gson.*
import io.ktor.application.ApplicationCallPipeline
import io.ktor.application.call
import io.ktor.http.ContentType
import io.ktor.http.HttpMethod
import io.ktor.http.HttpStatusCode
import io.ktor.request.httpMethod
import io.ktor.request.queryString
import io.ktor.request.receiveText
import io.ktor.request.uri
import io.ktor.response.respond
import io.ktor.response.respondText
import io.ktor.routing.Route
import io.ktor.routing.get
import io.ktor.routing.route
import io.ktor.routing.routing
import io.ktor.server.cio.CIO
import io.ktor.server.engine.ApplicationEngine
import io.ktor.server.engine.embeddedServer
import io.ktor.util.AttributeKey
import kuick.json.Json
import kuick.json.Json.gson
import kuick.logging.LogLevel
import kuick.logging.Logger
import kuick.logging.config
import kuick.logging.info
import kuick.orm.clazz
import kotlin.reflect.KFunction
import kotlin.reflect.full.callSuspend
import kotlin.reflect.full.declaredMemberFunctions

data class RpcServerDescriptor(
    val name: String,
    val port: Int,
    val services: List<String>
)

data class RpcServer(
    val name: String,
    val port: Int,
    val services: List<Any>
) {
    val descriptor: RpcServerDescriptor

    private val logger = Logger(RpcServer::class.simpleName!!)
    private lateinit var tserver: ApplicationEngine

    init {
        if (services.isEmpty()) throw IllegalArgumentException("Can't start KuickRPC servic without any service!!!")
        descriptor = RpcServerDescriptor(
            name,
            port,
            services = services.map { it.javaClass.interfaces.first().simpleName }
        )
    }

    fun start(wait: Boolean, debug: Boolean = false) {
        tserver = embeddedServer(CIO, port) {

            if (debug) {
                logger.config.enabled = true
                logger.config.minLevel = LogLevel.TRACE
                intercept(ApplicationCallPipeline.Features) {
                    val callUri = call.request.uri
                    println("${call.request.httpMethod.value} $callUri")
                    proceed()
                }
            }

            routing {
                // Descriptor
                get("/") {
                    val resultJson = Json.toJson(descriptor)
                    call.respondText(resultJson, ContentType.Application.Json)
                }

                // Service RPC
                val route = this
                logger.info { "Registering RPC services:" }
                services.forEach { register(it, route)  }
            }
        }
        logger.info { "$name (${RpcServer::class.simpleName} 1.1) started at $port" }
        tserver.start(wait)
    }

    fun stop() {
        tserver.stop(10L, 1000L)
    }

    private fun register(service: Any, route: Route) {
        service.visitRPC { srvName, method ->
            val path = "/$srvName/${method.name}"
            logger.info { "   $path -> $method" } // logging
            route.route(path) {
                handle {
                    val requestBody = when(call.request.httpMethod) {
                        HttpMethod.Get ->
                            call.request.queryParameters["args"]
                                ?: call.request.queryString()
                        else -> call.receiveText()
                    }

                    val rpcContext = call.request.headers[RPC_CONTEXT_HEADER]
                        ?.let { Json.fromJson(it, RpcContext::class) }
                        ?: RpcContext.random()

                    withRpcContext(rpcContext) {

                        logger.traceRpc { "POST $path with $rpcContext" }
                        //call.request.headers.flattenForEach { k, v ->
                        //    logger.traceRpc { "  HEADER $k: $v" }
                        //}
                        logger.traceRpc {"  ==> $requestBody"}

                        val res = JsonParser().parse(requestBody)
                        val jsonArray = when (res) {
                            is JsonNull -> JsonArray()
                            is JsonArray -> res
                            else -> throw IllegalArgumentException()
                        }

                        val args = buildArgsFromArray(
                            method,
                            jsonArray,
                            call.attributes.allKeys.map { it.name to call.attributes[it as AttributeKey<Any>] }.toMap()
                        )

                        try {
                            val beginAt = System.currentTimeMillis()
                            val result = method.callSuspend(service, *args.toTypedArray())
                            val resultJson = gson.toJson(result)
                            val endAt = System.currentTimeMillis()
                            val lapse = endAt - beginAt
                            logger.traceRpc {"  <== $resultJson ($lapse ms)"}

                            call.respondText(resultJson, ContentType.Application.Json)
                        } catch (t: Throwable) {
                            t.printStackTrace()
                            call.respond(HttpStatusCode.InternalServerError, t)
                        }
                    }

                }
            }
        }

    }

    private fun Any.visitRPC(opAction: (String, KFunction<*>) -> Unit) {
        val ifaces = javaClass.interfaces

        ifaces.forEach { iface ->
            val srvName = iface.simpleName
            logger.info { ">> $srvName" }
            iface.kotlin.declaredMemberFunctions.forEach { function ->
                try {
                    opAction(srvName, function)
                } catch (ieme: Throwable) {
                    println("WARN: invalid public method in controller: $function")
                    ieme.printStackTrace()
                }
            }
        }
    }

    private fun <T> buildArgsFromArray(handler: KFunction<T>,
                               requestParameters: JsonElement,
                               extraArgs: Map<String, Any>): Collection<Any?> {
        val requestParameters = when (requestParameters) {
            is JsonNull -> JsonArray()
            is JsonPrimitive -> if (requestParameters.asString.isEmpty()) JsonArray() else throw IllegalArgumentException()
            is JsonArray -> requestParameters
            else -> throw IllegalArgumentException()
        }
        return buildArgs(handler, requestParameters, extraArgs)
    }


    private fun <T> buildArgs(handler: KFunction<T>,
                              requestParameters: JsonElement,
                              extraArgs: Map<String, Any>): Collection<Any?> {
        val parameters = handler.parameters.drop(1)
        return parameters
            .withIndex()
            .map { (i, parameter) ->

                fun getParameter(): JsonElement? = when {
                    requestParameters.isJsonArray -> requestParameters.asJsonArray[i]
                    requestParameters.isJsonObject -> requestParameters.asJsonObject[parameter.name]
                    else -> null
                }

                val type = parameter.type.clazz.java
                when {
                    // If there's an extra parameter, overload it to whatever is sent by client argument
                    extraArgs[parameter.name] != null -> { extraArgs[parameter.name] }

                    type.isAssignableFrom(String::class.java) -> {
                        val jsonParam = getParameter()
                        val value = when (jsonParam) {
                            is JsonNull, null -> null
                            else -> jsonParam.asString
                        }
                        value
                    }

                    else -> {
                        try {
                            val jsonParam = getParameter()
                            val value = when (jsonParam) {
                                is JsonNull, null -> null
                                else -> gson.fromJson(jsonParam.toString(), type)
                            }
                            value
                        } catch (t: Throwable) {
                            extraArgs[parameter.name] ?: throw IllegalArgumentException("Missing expected field $type", t)
                        }
                    }
                }
            }
    }
}
