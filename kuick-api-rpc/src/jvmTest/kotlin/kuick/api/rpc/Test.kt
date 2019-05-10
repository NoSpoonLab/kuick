package kuick.api.rpc

import io.ktor.http.HttpMethod
import io.ktor.routing.routing
import io.ktor.server.testing.TestApplicationEngine
import io.ktor.server.testing.handleRequest
import io.ktor.server.testing.withTestApplication
import junit.framework.Assert.assertEquals
import kuick.di.Guice
import kuick.di.bindPerCoroutineJob
import org.junit.Test
import javax.inject.Singleton

class Test {

    data class Resource(
            val id: String,
            val field1: String,
            val field2: Int,
            val otherResource: String
    )

    @Singleton
    class ResourceApi {
        private val map = mapOf(
                "resource-id-1" to Resource(
                        id = "resource-id-1",
                        field1 = "test1",
                        field2 = 10,
                        otherResource = "example-id-2"
                ),
                "resource-id-2" to Resource(
                        id = "resource-id-2",
                        field1 = "test2",
                        field2 = 11,
                        otherResource = "example-id-2"
                )
        )

        fun getOne(id: String): Resource = map[id] ?: throw RuntimeException("404")

        fun getAll(): List<Resource> = map.values.toList()
    }


    private fun rpcTest(block: TestApplicationEngine.() -> Unit) {
        val injector = Guice {
            bindPerCoroutineJob()
        }
        withTestApplication {
            application.routing {
                rpcRoute<ResourceApi>(injector)
            }

            block()
        }
    }

    @Test
    fun test() = rpcTest {
        assertEquals("[{\"id\":\"resource-id-1\",\"field1\":\"test1\",\"field2\":10,\"otherResource\":\"example-id-2\"},{\"id\":\"resource-id-2\",\"field1\":\"test2\",\"field2\":11,\"otherResource\":\"example-id-2\"}]",
                handleRequest(HttpMethod.Post, "/rpc/ResourceApi/getAll").response.content)
    }

}
