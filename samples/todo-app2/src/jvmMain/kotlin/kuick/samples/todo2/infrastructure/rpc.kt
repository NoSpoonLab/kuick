package kuick.samples.todo2.infrastructure

import com.google.gson.*
import kuick.models.Id
import kuick.samples.todo2.infrastructure.reflection.invokeWithParams
import java.lang.reflect.Method
import java.lang.reflect.Parameter
import java.lang.reflect.Type
import kotlin.coroutines.Continuation
import kotlin.coroutines.suspendCoroutine


fun Any.visitRPC(opAction: (String, Method) -> Unit) {
    val ifaces = javaClass.interfaces

    ifaces.forEach { iface ->
        val srvName = iface.simpleName
        val methods = iface.declaredMethods

        methods.forEach { method ->
            try {
                opAction(srvName, method)
            } catch (ieme: Throwable) {
                println("WARN: invalid public method in controller: $method")
                ieme.printStackTrace()
            }
        }
    }

}

class IllegalRPCBody : Exception()

suspend fun invokeRPC(paramsJson: String, method: Method, obj: Any, extraArgs: List<Any> = emptyList()): Any? {
    val res = JsonParser().parse(paramsJson)
    val jsonArray = when (res) {
        is JsonNull -> JsonArray()
        is JsonArray -> res as JsonArray
        else -> throw IllegalRPCBody()
    }

    //println("POST $finalEndpointPath")
    //println("    --> ${bodyArgs}")
    return suspendCoroutine { c ->
        val args = buildArgs(method.parameters, jsonArray, c, extraArgs.associateBy { it::class.java })
        //args.forEach { println(" - ${it}") }
        //println("1 - Invocando...***")
        try {
            val result = obj.invokeWithParams(method, args)
            //println("2 - Result: $result")
            c.resumeWith(Result.success(result))
        } catch (t: Throwable) {
            c.resumeWith(Result.failure(t))
        }
    }
}

private fun buildArgs(parameters: Array<Parameter>,
                      bodyValues: JsonArray,
                      c: Continuation<Any>,
                      extraArgs: Map<Class<out Any>, Any>): Collection<Any?> {
    return parameters.withIndex().map { (i, parameter) ->
        val type = parameter.type
        //println("ARG[${type.simpleName}]")
        when {
            type.isAssignableFrom(Continuation::class.java) -> c
            type.isAssignableFrom(String::class.java) -> {
                val jsonParam = bodyValues[i]
                val value = when(jsonParam) {
                    is JsonNull, null -> null
                    else -> jsonParam.asString
                }
                value
            }
            // If there's an extra parameter, overload it to whatever is sent by client argument
            extraArgs.get(type) != null -> extraArgs.get(type)
            else -> {
                try {
                    val jsonParam = bodyValues[i]
                    val value = when(jsonParam) {
                        is JsonNull, null -> extraArgs.get(type)
                        else -> gson.fromJson(jsonParam.toString(), type)
                    }
                    value
                } catch (t: Throwable) {
                    extraArgs.get(type) ?: throw IllegalArgumentException("Missing expected field ${type}", t)
                }
            }
        }
    }
}

private val gson: Gson = GsonBuilder().registerTypeHierarchyAdapter(Id::class.java, IdGsonAdapter()).create()

class IdGsonAdapter : JsonDeserializer<Id>, JsonSerializer<Id> {

    override fun deserialize(je: JsonElement, type: Type, ctx: JsonDeserializationContext): Id {
        val constuctor = (type as Class<*>).declaredConstructors.first()
        return constuctor.newInstance(je.asString) as Id
    }

    override fun serialize(id: Id?, type: Type, ctx: JsonSerializationContext): JsonElement {
        return JsonPrimitive(id?.id)
    }

}

