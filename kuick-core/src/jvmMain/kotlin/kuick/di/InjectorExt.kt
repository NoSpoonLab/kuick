package kuick.di

import com.google.inject.*
import kotlinx.coroutines.*
import kuick.core.*
import kuick.utils.*
import kotlin.coroutines.*

fun Guice(callback: Binder.() -> Unit): Injector = Guice.createInjector(object : Module {
    override fun configure(binder: Binder) = callback(binder)
})

val Binder.registeredModules by WeakProperty { LinkedHashSet<Module>() }

abstract class GuiceModule(vararg val dependencies: Module) : Module {
    abstract fun Binder.registerBindings()
    final override fun configure(binder: Binder) {
        // Only register this module if not registered previously
        if (!binder.registeredModules.contains(this)) {
            binder.registeredModules += this
            // Register dependencies
            for (dependency in dependencies) dependency.configure(binder)
            binder.registerBindings()
        }
    }
}

fun GuiceModule(vararg dependencies: Module, callback: Binder.() -> Unit) = object : GuiceModule(*dependencies) {
    override fun Binder.registerBindings() = callback()
}

inline fun Binder.bind(module: Module): Binder = this.apply { module.configure(this) }

inline fun <reified T> Binder.bind(instance: T): Binder = this.apply {
    bind(T::class.java).toInstance(instance)
}

inline fun <reified T, reified R : T> Binder.bind(): Binder = this.apply {
    bind(T::class.java).to(R::class.java)
}

inline fun <reified T> Binder.bindSelf(): Binder = this.apply {
    bind(T::class.java).asEagerSingleton()
}

class InjectorNotInContextException : RuntimeException()

class InjectorContext(val injector: Injector) : AbstractCoroutineContextElement(Key) {
    companion object Key : CoroutineContext.Key<InjectorContext>
}

@KuickInternal
suspend fun injector(): Injector = coroutineContext[InjectorContext]?.injector ?: throw InjectorNotInContextException()

inline fun <reified T : Any> Injector.get() = this.getInstance(T::class.java)
inline fun <reified T : Any> Injector.get(callback: T.() -> Unit) = this.getInstance(T::class.java).apply(callback)
inline fun <reified T : Any> Injector.getOrNull() = try {
    get<T>()
} catch (e: Throwable) {
    null
}

suspend fun <T> withInjectorContext(injector: Injector, callback: suspend CoroutineScope.() -> T) = withContext(InjectorContext(injector), callback)
