package kuick.http

import kotlinx.coroutines.*
import kotlin.test.*

/*
class RouteRegistrationTest {
    @Test
    fun test() {
        runBlocking {
            val server = TestHttpServer()
            server.registerRoutesInClass(MyRoutes())
            assertEquals("HELLO WORLD!", server.request("/")?.retrieveBody())
            //assertEquals("HELLO test!", server.request("/test?name=test")?.retrieveBody())
        }
    }

    class MyRoutes {
        @Route("/") suspend fun root() = "HELLO WORLD!"
        //@Route("/test") suspend fun test(@Get name: String) = "HELLO $name!"
    }
}
*/
