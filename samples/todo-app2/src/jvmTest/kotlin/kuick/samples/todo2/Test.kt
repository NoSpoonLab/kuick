package kuick.samples.todo2

import io.ktor.http.ContentType
import io.ktor.http.HttpHeaders
import io.ktor.http.HttpMethod
import io.ktor.routing.routing
import io.ktor.server.testing.TestApplicationEngine
import io.ktor.server.testing.handleRequest
import io.ktor.server.testing.setBody
import io.ktor.server.testing.withTestApplication
import junit.framework.Assert.assertEquals
import kotlinx.coroutines.runBlocking
import kuick.client.db.DbClientPool
import kuick.client.jdbc.JdbcDriver
import kuick.di.Guice
import kuick.di.bindPerCoroutineJob
import kuick.ktor.installContextPerRequest
import org.junit.Test

class Test {

    private val injector = Guice {
        bindPerCoroutineJob()
        configure()
    }

    private fun restTest(block: TestApplicationEngine.() -> Unit) {
        runBlocking {
            withTestApplication {
                application.installContextPerRequest(injector, DbClientPool { JdbcDriver.connectMemoryH2() }) {
                    injector.getInstance(TodoRepository::class.java).init()
                }
                application.routing {
                    restRouting(injector)
                }

                block()
            }
        }
    }

    @Test
    fun test() = restTest {
        assertEquals("[]", handleRequest(HttpMethod.Get, "/todos").response.content)
    }

    @Test
    fun test2() = restTest {
        val content = handleRequest(HttpMethod.Post, "/todos") {
            addHeader(HttpHeaders.ContentType, ContentType.Application.Json.toString())
            setBody("{\"text\":\"test\"}")
        }.response
        assertEquals("[{\"text\":\"test\"}]", handleRequest(HttpMethod.Get, "/todos?\$fields=[text]").response.content)
    }

}
