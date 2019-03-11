package kuick.repositories.patterns

import kuick.bus.Bus
import kotlin.reflect.KClass

enum class ModelChangeType { INSERT, UPDATE, DELETE, INIT }

fun <T:Any> KClass<T>.changeEventTopic(type: ModelChangeType) = "${simpleName}/${type.name}"

suspend fun <T:Any> Bus.publishInsert(ev: T) =
        publishAsync(ev::class.changeEventTopic(ModelChangeType.INSERT), ev)

suspend fun <T:Any> Bus.publishUpdate(ev: T) =
        publishAsync(ev::class.changeEventTopic(ModelChangeType.UPDATE), ev)
