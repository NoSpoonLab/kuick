package kuick.utils

import org.junit.Test
import kotlin.test.*

class WeakPropertyTest {
    class Demo()

    var Demo.test by WeakProperty { 10 }

    @Test
    fun test() {
        val a = Demo()
        val b = Demo()

        assertEquals(10, a.test)
        a.test = 20
        b.test = 30
        assertEquals(20, a.test)
        assertEquals(30, b.test)
    }
}