package kuick.api.rpc

import kotlin.reflect.KFunction
import kotlin.reflect.full.memberFunctions


fun Any.visitRPC(opAction: (String, KFunction<*>) -> Unit) {
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

