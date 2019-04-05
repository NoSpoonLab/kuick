package kuick.ktor

import com.google.inject.*
import io.ktor.application.*
import io.ktor.util.*
import kotlinx.coroutines.*
import kuick.di.*

class GuiceFeature(val injector: Injector, val perRequestInjectorDecorator: (Injector) -> Injector) {
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

    fun withRootInjector(callback: suspend () -> Unit) = runBlocking {
        withInjectorContext(injector) { callback() }
    }

    fun withInjector(callback: suspend () -> Unit) = runBlocking {
        withInjectorContext(createRequestInjector()) { callback() }
    }

    companion object Feature : ApplicationFeature<ApplicationCallPipeline, Configuration, GuiceFeature> {
        override val key = AttributeKey<GuiceFeature>("Guice")
        override fun install(pipeline: ApplicationCallPipeline, configure: Configuration.() -> Unit): GuiceFeature {
            val config = Configuration().apply { configure() }
            val feature = GuiceFeature(config.injector, config.injectorDecorator)
            pipeline.intercept(ApplicationCallPipeline.Call) {
                withInjectorContext(feature.createRequestInjector()) {
                    this@intercept.proceed()
                }
            }
            return feature
        }
    }
}
