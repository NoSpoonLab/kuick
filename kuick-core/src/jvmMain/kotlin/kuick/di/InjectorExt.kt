package kuick.di

import com.google.inject.*
import kotlinx.coroutines.*
import kotlin.coroutines.*

fun Guice(callback: Binder.() -> Unit): Injector = Guice.createInjector(object : Module {
    override fun configure(binder: Binder) = callback(binder)
})

inline fun <reified T> Binder.bindToInstance(instance: T): Binder = this.apply {
    bind(T::class.java).toInstance(instance)
}

inline fun <reified T, reified R : T> Binder.bindToType(): Binder = this.apply {
    bind(T::class.java).to(R::class.java)
}

class InjectorNotInContextException : RuntimeException()

class InjectorContext(val injector: Injector) : AbstractCoroutineContextElement(Key) {
    companion object Key : CoroutineContext.Key<InjectorContext>
}

suspend fun injector(): Injector = coroutineContext[InjectorContext]?.injector ?: throw InjectorNotInContextException()

inline fun <reified T : Any> Injector.get(callback: T.() -> Unit = {}) = this.getInstance(T::class.java).apply(callback)
inline fun <reified T : Any> Injector.getOrNull() = try { get<T>() } catch (e: Throwable) { null }

suspend fun <T> withInjectorContext(injector: Injector, callback: suspend CoroutineScope.() -> T) = withContext(InjectorContext(injector), callback)
