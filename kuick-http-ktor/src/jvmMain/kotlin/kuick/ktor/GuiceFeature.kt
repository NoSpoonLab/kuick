package kuick.ktor

import com.google.inject.*
import io.ktor.application.*
import io.ktor.util.*
import kotlinx.coroutines.*
import kuick.di.*

class GuiceFeature(val injector: Injector) {
    class Configuration {
        internal lateinit var injector: Injector

        fun injector(injector: Injector) = run { this.injector = injector }
        fun injector(block: Binder.() -> Unit) = injector(Guice(block))
    }

    fun withInjector(callback: suspend () -> Unit) = runBlocking {
        withInjectorContext(injector) {
            callback()
        }
    }

    companion object Feature : ApplicationFeature<ApplicationCallPipeline, Configuration, GuiceFeature> {
        override val key = AttributeKey<GuiceFeature>("Guice")
        override fun install(pipeline: ApplicationCallPipeline, configure: Configuration.() -> Unit): GuiceFeature {
            val feature = GuiceFeature(Configuration().apply { configure() }.injector)
            pipeline.intercept(ApplicationCallPipeline.Call) {
                withInjectorContext(feature.injector) {
                    this@intercept.proceed()
                }
            }
            return feature
        }
    }
}
