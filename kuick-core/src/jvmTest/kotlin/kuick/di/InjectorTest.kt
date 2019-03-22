package kuick.di

import org.junit.Test
import kotlin.test.*

class InjectorTest {
    @Test
    fun test() {
        class Dep1
        class Dep2

        val module1 = GuiceModule { bind(Dep1()) }
        val module2 = GuiceModule(module1) { bindSelf<Dep2>() }

        val injector1 = Guice { bind(module2) }
        val injector2 = Guice { bind(module2).bind(module1) } // Shouldn't be re-registered

        assertNotNull(injector1.get<Dep1>())
        assertNotNull(injector1.get<Dep2>())

        assertNotNull(injector2.get<Dep1>())
        assertNotNull(injector2.get<Dep2>())
    }
}