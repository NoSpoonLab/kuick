package kuick.di

import kotlinx.coroutines.*
import kuick.core.*
import kotlin.test.*

@UseExperimental(KuickInternal::class)
class PerCoroutineJobTest {
    @Test
    fun test() {
        runBlocking {
            val injector = Guice { bindPerCoroutineJob() }
            var executed = 0
            injector.get<PerCoroutineJob>().runSuspending {
                executed++
            }
            injector.get<PerCoroutineJob>().register { callback ->
                withInjectorContextNoIntercepted(injector) {
                    callback()
                }
            }
            injector.get<PerCoroutineJob>().runSuspending {
                assertNotNull(injector())
                executed++
            }
            assertEquals(2, executed)
        }
    }
}