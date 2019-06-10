package kuick.repositories

import java.util.*
import kotlin.reflect.KClass

actual fun <V : Any> isBasicType(clazz: KClass<V>): Boolean = setOf(Boolean::class, Int::class, Long::class, Float::class, Double::class, String::class, Char::class, UUID::class).any { it == clazz }