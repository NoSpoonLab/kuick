package kuick.concurrent.async

import kotlinx.coroutines.*
import kotlin.system.*
import kotlin.test.*

class NamedAsyncThreadsTest {
    @Test
    fun test() {
        runBlocking {
            val time = measureTimeMillis {
                val named = NamedAsyncThreads()
                var log = ""
                val def1 = async(start = CoroutineStart.UNDISPATCHED) { named("test") { log += "1"; delay(100L) } }
                val def2 = async(start = CoroutineStart.UNDISPATCHED) { named("test") { log += "2"; delay(100L) } }
                val def3 = async(start = CoroutineStart.UNDISPATCHED) { named("test2") { log += "3"; delay(100L) } }
                def1.await()
                def2.await()
                def3.await()
                assertEquals("132", log) // Checks the execution order
                assertEquals(0, named.threadsCount()) // Check that no leaks happen once everything is done
            }
            assertTrue(time >= 200) // Checks that def1 and def2 were executed one after another
        }
    }
}