package kuick.di

import kotlinx.coroutines.*
import kuick.core.*
import kotlin.test.*

@UseExperimental(KuickInternal::class)
class PerCoroutineJobServiceTest {
    @Test
    fun test() {
        runBlocking {
            val injector = Guice { bindPerCoroutineJobService() }
            var executed = 0
            injector.get<PerCoroutineJobService>().execute {
                executed++
            }
            injector.get<PerCoroutineJobService>().register { callback ->
                withInjectorContext(injector) {
                    callback()
                }
            }
            injector.get<PerCoroutineJobService>().execute {
                assertNotNull(injector())
                executed++
            }
            assertEquals(2, executed)
        }
    }
}