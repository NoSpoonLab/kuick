package kuick.ktor

import com.google.inject.*
import io.ktor.application.*
import io.ktor.util.*
import kuick.di.*

class PerCoroutineJobFeature(val perCoroutineJob: PerCoroutineJob) : ApplicationFeature<ApplicationCallPipeline, PerCoroutineJobFeature, PerCoroutineJobFeature> {
    constructor(injector: Injector) : this(injector.get<PerCoroutineJob>())

    companion object {
        val key = AttributeKey<PerCoroutineJobFeature>("PerCoroutineJobFeature")
    }

    override val key = PerCoroutineJobFeature.key

    override fun install(pipeline: ApplicationCallPipeline, configure: PerCoroutineJobFeature.() -> Unit): PerCoroutineJobFeature {
        pipeline.intercept(ApplicationCallPipeline.Call) {
            perCoroutineJob.injector.runWithInjector {
                this@intercept.proceed()
            }
        }
        return this
    }
}
