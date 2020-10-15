package kuick.caching

import kuick.json.Json
import kuick.utils.AsyncCloseable
import kuick.utils.Named
import kotlin.reflect.KClass

fun <T : Any> Cache<String, String>.typeWithJsonNullable(clazz: KClass<T>): Cache<String, T?> {
    val parent = this
    return object : Cache<String, T?>, Invalidable<String> by parent, AsyncCloseable by parent, Named by parent {
        override suspend fun get(key: String, builder: suspend (key: String) -> T?): T? =
            Json.fromJsonNullable(parent.get(key) { Json.toJson(builder(it)) }, clazz)
    }
}

fun <T : Any> Cache<String, String>.typeWithJson(clazz: KClass<T>): Cache<String, T> {
    val parent = this
    return object : Cache<String, T>, Invalidable<String> by parent, AsyncCloseable by parent, Named by parent {
        override suspend fun get(key: String, builder: suspend (key: String) -> T): T =
            Json.fromJson(parent.get(key) { Json.toJson(builder(it)) }, clazz)
    }
}

inline fun <reified T : Any> Cache<String, String>.typeWithJson(): Cache<String, T> = typeWithJson(T::class)
inline fun <reified T : Any> Cache<String, String>.typeWithJsonNullable(): Cache<String, T?> = typeWithJsonNullable(T::class)
