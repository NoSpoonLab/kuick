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
import kotlin.reflect.KFunction
import kotlin.reflect.full.memberFunctions


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

    private fun Any.visitRPC(opAction: (String, KFunction<*>) -> Unit) {
        //TODO previous version on visitRPC did iterated over interfaces, not sure if it's necessarry
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


//private val gson: Gson = GsonBuilder().registerTypeHierarchyAdapter(Id::class.java, IdGsonAdapter()).create()
//
//class IdGsonAdapter : JsonDeserializer<Id>, JsonSerializer<Id> {
//
//    override fun deserialize(je: JsonElement, type: Type, ctx: JsonDeserializationContext): Id {
//        val constuctor = (type as Class<*>).declaredConstructors.first()
//        return constuctor.newInstance(je.asString) as Id
//    }
//
//    override fun serialize(id: Id?, type: Type, ctx: JsonSerializationContext): JsonElement {
//        return JsonPrimitive(id?.id)
//    }
//
//}

