package kuick.ktor

import com.google.inject.*
import io.ktor.application.*
import io.ktor.util.*
import kotlinx.coroutines.*
import kuick.di.*

class GuiceFeature(baseInjector: Injector, val perRequestInjectorDecorator: (Injector) -> Injector) {
    val injector = baseInjector.createChildInjector(GuiceModule { bind(this@GuiceFeature) })
    fun createRequestInjector() = perRequestInjectorDecorator(injector)

    class Configuration {
        internal lateinit var injector: Injector
        internal var injectorDecorator: (Injector) -> Injector = {it}

        fun injector(injector: Injector) = run { this.injector = injector }
        fun injector(block: Binder.() -> Unit) = injector(Guice(block))

        fun perRequestInjectorDecorator(injectorDecorator: (Injector) -> Injector) {
            this.injectorDecorator = injectorDecorator
        }
    }

    suspend fun withRootInjector(callback: suspend (injector: Injector) -> Unit) = withInjectorContext(injector) { callback(injector) }
    suspend fun withPreRequestInjector(callback: suspend (injector: Injector) -> Unit) = createRequestInjector().let { injector -> withInjectorContext(injector) { callback(injector) } }

    fun runBlockingWithRootInjector(callback: suspend (injector: Injector) -> Unit) = runBlocking { withRootInjector(callback) }
    fun runBlockingWithPreRequestInjector(callback: suspend (injector: Injector) -> Unit) = runBlocking { withPreRequestInjector(callback) }

    @Deprecated("", ReplaceWith("runBlockingWithPreRequestInjector(callback)"))
    fun withInjector(callback: suspend (injector: Injector) -> Unit) = runBlockingWithPreRequestInjector(callback)

    companion object Feature : ApplicationFeature<ApplicationCallPipeline, Configuration, GuiceFeature> {
        override val key = AttributeKey<GuiceFeature>("Guice")
        override fun install(pipeline: ApplicationCallPipeline, configure: Configuration.() -> Unit): GuiceFeature {
            val config = Configuration().apply { configure() }
            val feature = GuiceFeature(config.injector, config.injectorDecorator)
            pipeline.intercept(ApplicationCallPipeline.Call) {
                feature.withPreRequestInjector() {
                    this@intercept.proceed()
                }
            }
            return feature
        }
    }
}
