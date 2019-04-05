package kuick.ktor

import io.ktor.application.*
import io.ktor.util.*
import kuick.di.*

class PerCoroutineJobFeature(val perCoroutineJob: PerCoroutineJob) : ApplicationFeature<ApplicationCallPipeline, PerCoroutineJobFeature, PerCoroutineJobFeature> {
    companion object {
        val key = AttributeKey<PerCoroutineJobFeature>("PerCoroutineJobFeature")
    }

    override val key = PerCoroutineJobFeature.key

    override fun install(pipeline: ApplicationCallPipeline, configure: PerCoroutineJobFeature.() -> Unit): PerCoroutineJobFeature {
        pipeline.intercept(ApplicationCallPipeline.Call) {
            perCoroutineJob.runSuspending {
                this@intercept.proceed()
            }
        }
        return this
    }
}
