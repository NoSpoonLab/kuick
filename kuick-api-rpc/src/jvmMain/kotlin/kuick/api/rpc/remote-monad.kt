package kuick.api.rpc


import arrow.core.Id
import arrow.core.extensions.`try`.monad.binding
import arrow.core.fix
import arrow.data.Reader
import kuick.api.buildArgsFromArray
import kuick.api.toJson
import kuick.api.toJsonArray
import kuick.json.Json
import kotlin.reflect.KClass
import kotlin.reflect.KFunction
import kotlin.reflect.full.declaredFunctions
import kotlin.reflect.jvm.jvmName


interface Monad<T> {
    fun flatMap(function: (T) -> Monad<T>): Monad<T>
}

typealias Remote = Reader<Device, String?>

interface RemoteMonad<T> {
    val value: T
    fun flatMap(function: (T) -> RemoteMonad<T>): RemoteMonad<T>
}

//class Remote(override val value: String?, val device: Device) : RemoteMonad<String?> {
//    override fun flatMap(function: (String?) -> RemoteMonad<String?>): RemoteMonad<String?> {
//        sendCommand(device, )
//    }
//}

private inline fun <reified T : Any> sendCommand(command: Command<T>): Remote = Remote { device ->
    Id(
            if (T::class.isInstance(Unit)) {
                device.async(command.serialize())
                null
            } else {
//        Json.fromJson(
                device.sync(command.serialize())
//        )
            }
    )
}


class Command<T>(
        val serviceClass: KClass<out Any>,
        val method: KFunction<T>,
        val params: Collection<Any?>
) {
    fun serialize(): String = "${serviceClass.jvmName}/${method.name}/${params.map { "$it;" }}"

    companion object {
        fun <T> deserialize(value: String): Command<T> {
            val (serviceClassName, methodName, paramsString) = value.split("/")
            val service = Class.forName(serviceClassName.substringBefore("Client")).kotlin
            val method = service.declaredFunctions.find { it.name == methodName } as KFunction<T>
                    ?: throw RuntimeException("TODO") // TODO
            val paramsJson = paramsString.split(";").toJson().toJsonArray() //TODO
            val params = buildArgsFromArray(method, paramsJson, emptyMap())
            return Command(service, method, params)
        }
    }
}

interface Device {
    fun async(command: String): Unit
    fun sync(command: String): String //TODO change to T
}

fun send(device: Device, remote: Remote): String? = remote.run(device).fix().extract()

//suspend fun send(device: Device, remote: suspend () -> Remote): String? = remote().run(device)

fun <T : Any> getInstance(clazz: KClass<T>): T =
        when (clazz) {
            ResourceApi::class -> ResourceApi()
            else -> throw RuntimeException("Service not found")
        } as T

class MockDevice : Device {
    override fun sync(command: String): String {
        val command = Command.deserialize<Any>(command)
        val api = getInstance(command.serviceClass)
        return Json.toJson(
                command.method.call(api, *command.params.toTypedArray())
        )
    }

    override fun async(command: String): Unit {
        val command = Command.deserialize<Any>(command)
        val api = getInstance(command.serviceClass)
        command.method.call(api, *command.params.toTypedArray())
    }

}


data class Resource(
        val id: Int,
        val value: String
)

class ResourceApi {
    fun getOne(): Resource {
        return Resource(1, "someTest")
    }

    fun printSmth(): Unit {
        println("Smth")
    }
}


class ResourceApiClient {
    suspend fun getOne(): Remote = sendCommand(Command(this::class, ResourceApi::getOne, emptyList()))
    fun printSmth(): Remote = sendCommand(Command(this::class, ResourceApi::printSmth, emptyList()))
}


suspend fun main() {
    val device = MockDevice()

    val api = ResourceApiClient()

//    val send = send(device, api.getOne())
//    send(device,
//            api.printSmth().flatMap {
//                api.getOne()
//            }.flatMap { a ->
//                println(a)
//                api.printSmth()
//            }
//    )

    binding {
        api.printSmth()
//        val (a) = api.getOne()
//        println(a)
        api.printSmth()
        api.printSmth()
        api.printSmth()
        api.printSmth()
    }.fold({}, { it.run(device) })

//            .fold({
//        throw it
//    },{
//        it
//    })
//    )


    println()


//
//    send(device) {
//        api.printSmth()
//        api.getOne()
//        api.printSmth()
//    }
}


