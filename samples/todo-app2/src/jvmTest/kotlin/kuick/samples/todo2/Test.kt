package kuick.samples.todo2

import com.google.gson.JsonParser
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


    private fun restTest(block: TestApplicationEngine.() -> Unit) {
        val injector = Guice {
            bindPerCoroutineJob()
            configure()
        }
        runBlocking {
            withTestApplication {
                application.installContextPerRequest(injector, DbClientPool { JdbcDriver.connectMemoryH2() }) {
                    injector.getInstance(TodoRepository::class.java).init()
                    injector.getInstance(UserRepository::class.java).init()
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
    fun field_test() = restTest {

        val addedUserId = addUser()

        handleRequest(HttpMethod.Post, "/todos") {
            addHeader(HttpHeaders.ContentType, ContentType.Application.Json.toString())
            setBody("{\"text\":\"test\",\"owner\":\"$addedUserId\"}")
        }

        assertEquals("[{\"text\":\"test\"}]", handleRequest(HttpMethod.Get, "/todos?\$fields=[text]").response.content)
    }

    private fun TestApplicationEngine.addUser(): String? {
        val jsonParser = JsonParser()

        val userAddResponse = handleRequest(HttpMethod.Post, "/users") {
            addHeader(HttpHeaders.ContentType, ContentType.Application.Json.toString())
            setBody("{\"name\":\"Marcin\"}")
        }.response.content

        val addedUserId = jsonParser.parse(userAddResponse).asJsonObject["id"].asString
        return addedUserId
    }

    @Test
    fun include_test() = restTest {

        val addedUserId = addUser()

        handleRequest(HttpMethod.Post, "/todos") {
            addHeader(HttpHeaders.ContentType, ContentType.Application.Json.toString())
            setBody("{\"text\":\"test\",\"owner\":\"$addedUserId\"}")
        }

        assertEquals(
                "[{\"owner\":{\"id\":\"$addedUserId\",\"name\":\"Marcin\"}}]",
                handleRequest(HttpMethod.Get, "/todos?\$fields=[owner]&\$include=[owner]").response.content
        )
    }

}
