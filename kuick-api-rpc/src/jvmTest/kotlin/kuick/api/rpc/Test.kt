package kuick.api.rpc

import io.ktor.application.ApplicationCallPipeline
import io.ktor.application.call
import io.ktor.http.HttpMethod
import io.ktor.http.HttpStatusCode
import io.ktor.response.respond
import io.ktor.routing.Route
import io.ktor.routing.route
import io.ktor.routing.routing
import io.ktor.server.testing.TestApplicationEngine
import io.ktor.server.testing.handleRequest
import io.ktor.server.testing.withTestApplication
import io.ktor.util.AttributeKey
import junit.framework.Assert.assertEquals
import kuick.api.parameters.include.InvalidIncludeParamException
import kuick.api.parameters.preserve.InvalidFieldParamException
import kuick.api.toJson
import kuick.di.Guice
import kuick.di.bindPerCoroutineJob
import org.junit.Test
import javax.inject.Singleton

class Test {

    data class Resource(
            val id: String,
            val field1: String,
            val field2: Int,
            val otherResource: String? = null
    )

    data class OtherResource(
            val id: String,
            val field1: String,
            val field2: Int,
            val otherResource2: String? = null
    )

    data class OtherResource2(
            val id: String,
            val field1: String
    )


    @Singleton
    class ResourceApi {
        private val map = mapOf(
                "resource-id-1" to Resource(
                        id = "resource-id-1",
                        field1 = "test1",
                        field2 = 10
                ),
                "resource-id-2" to Resource(
                        id = "resource-id-2",
                        field1 = "test2",
                        field2 = 11,
                        otherResource = "other-resource-id-2"
                )
        )

        fun getOne(id: String): Resource = map[id] ?: throw RuntimeException("404")

        fun getAll(): List<Resource> = map.values.toList()
        fun test(): String = "TEST"
    }

    @Singleton
    class OtherResourceApi {
        private val map = mapOf(
                "other-resource-id-2" to OtherResource(
                        id = "other-resource-id-2",
                        field1 = "test1",
                        field2 = 10,
                        otherResource2 = "other-resource-2-id-1"
                )
        )

        fun getOne(id: String): OtherResource = map[id] ?: throw RuntimeException("404")
    }

    @Singleton
    class OtherResource2Api {
        private val map = mapOf(
                "other-resource-2-id-1" to OtherResource2(
                        id = "other-resource-2-id-1",
                        field1 = "test1"
                )
        )

        fun getOne(id: String): OtherResource2 = map[id] ?: throw RuntimeException("404")
    }


    //TODO Pipeline : discuss
    private fun Route.withSomeCheck(path: String = "", build: Route.() -> Unit) = route(path) {
        intercept(ApplicationCallPipeline.Call) {
            if (false) {// e.g. if (!ADMIN_USERS.contains(session.userId)) {
                call.respond(HttpStatusCode.Forbidden)
                finish()
            }
        }
        build()
    }


    //TODO Providing additional parameters (later passed to handler method) discuss
    private fun Route.withSomeAdditionalParameter(path: String = "", build: Route.() -> Unit) = route(path) {
        intercept(ApplicationCallPipeline.Call) {
            call.attributes.put(AttributeKey("test"), "test")
        }
        build()
    }

    private fun rpcTest(block: TestApplicationEngine.() -> Unit) {
        val injector = Guice {
            bindPerCoroutineJob()
        }
        withTestApplication {
            application.routing {
                rpcRoute<ResourceApi>(injector){
                    withFieldsParameter()
                    withIncludeParameter(
                            // TODO before I tried to provide here: Resource::otherResource to OtherResourceApi::getOne -> discuss
                            Resource::otherResource to { id -> injector.getInstance(OtherResourceApi::class.java).getOne(id) },
                            OtherResource::otherResource2 to { id -> injector.getInstance(OtherResource2Api::class.java).getOne(id) }
                    )
                }

            }

            block()
        }
    }

    @Test
    fun test() = rpcTest {
        assertEquals(
                listOf(
                        mapOf(
                                "id" to "resource-id-1",
                                "field1" to "test1",
                                "field2" to 10
                        ),
                        mapOf(
                                "id" to "resource-id-2",
                                "field1" to "test2",
                                "field2" to 11,
                                "otherResource" to "other-resource-id-2"
                        )
                ).toJson(),
                handleRequest(HttpMethod.Post, "/rpc/ResourceApi/getAll").response.content
        )
    }

    @Test
    fun field_test() = rpcTest {
        assertEquals(
                listOf(
                        mapOf(
                                "field1" to "test1"
                        ),
                        mapOf(
                                "field1" to "test2"
                        )
                ).toJson(),
                handleRequest(HttpMethod.Post, "/rpc/ResourceApi/getAll?\$fields=[field1]").response.content
        )
    }

    @Test
    fun include_test() = rpcTest {
        assertEquals(
                listOf(
                        mapOf(
                                "id" to "resource-id-1"
                        ),
                        mapOf(
                                "id" to "resource-id-2",
                                "otherResource" to mapOf(
                                        "id" to "other-resource-id-2",
                                        "field1" to "test1",
                                        "field2" to 10,
                                        "otherResource2" to "other-resource-2-id-1"
                                )
                        )
                ).toJson(),
                handleRequest(HttpMethod.Post, "/rpc/ResourceApi/getAll?\$fields=[id,otherResource]&\$include=[otherResource]").response.content
        )
    }

    @Test(expected = InvalidFieldParamException::class)
    fun `should throw exception on wrongly defined fields parameter - when trying to preserve field of nested resource without preserving field itself`() = rpcTest {
        handleRequest(HttpMethod.Post, "/rpc/ResourceApi/getAll?\$fields=[id,otherResource.id]").response.content
    }

    @Test(expected = InvalidFieldParamException::class)
    fun `should throw exception on wrongly defined fields parameter - when trying to preserve field that don't exist in a model`() = rpcTest {
        handleRequest(HttpMethod.Post, "/rpc/ResourceApi/getAll?\$fields=[id,someField]").response.content
    }

    @Test(expected = InvalidIncludeParamException::class)
    fun `should throw exception on wrongly defined include parameter - when include is not supported for specified field`() = rpcTest {
        handleRequest(HttpMethod.Post, "/rpc/ResourceApi/getAll?\$include=[id]").response.content
    }

    @Test(expected = InvalidIncludeParamException::class)
    fun `should throw exception on wrongly defined include parameter - when trying to include field of nested resource without including resource itself`() = rpcTest {
        handleRequest(HttpMethod.Post, "/rpc/ResourceApi/getAll?\$include=[otherResource.id]").response.content
    }

    @Test(expected = InvalidIncludeParamException::class)
    fun `should throw exception on wrongly defined include parameter - when trying to include field that don't exist in a model`() = rpcTest {
        handleRequest(HttpMethod.Post, "/rpc/ResourceApi/getAll?\$include=[id,someField]").response.content
    }

    @Test
    fun `should handle nested fields in fields parameter`() = rpcTest {
        assertEquals(
                listOf(
                        mapOf(
                                "id" to "resource-id-1"
                        ),
                        mapOf(
                                "id" to "resource-id-2",
                                "otherResource" to mapOf(
                                        "id" to "other-resource-id-2"
                                )
                        )
                ).toJson(),
                handleRequest(HttpMethod.Post, "/rpc/ResourceApi/getAll?\$fields=[id,otherResource,otherResource.id]&\$include=[otherResource]").response.content
        )
    }

    @Test
    fun `should handle nested fields in include parameter`() = rpcTest {
        assertEquals(
                listOf(
                        mapOf(
                                "id" to "resource-id-1"
                        ),
                        mapOf(
                                "id" to "resource-id-2",
                                "otherResource" to mapOf(
                                        "id" to "other-resource-id-2",
                                        "field1" to "test1",
                                        "field2" to 10,
                                        "otherResource2" to mapOf(
                                                "id" to "other-resource-2-id-1",
                                                "field1" to "test1"
                                        )
                                )
                        )
                ).toJson(),
                handleRequest(HttpMethod.Post, "/rpc/ResourceApi/getAll?\$fields=[id,otherResource]&\$include=[otherResource,otherResource.otherResource2]").response.content
        )
    }
}
