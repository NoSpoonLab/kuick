package kuick.utils

import com.google.inject.Injector
import java.lang.reflect.Modifier

fun Class<*>.nonStaticFields() = declaredFields.filterNot { Modifier.isStatic(it.modifiers) }

inline fun <reified T : Any> Injector.get(callback: T.() -> Unit = {}) = this.getInstance(T::class.java).apply(callback)
inline fun <reified T : Any> Injector.getOrNull() = try { get<T>() } catch (e: Throwable) { null }