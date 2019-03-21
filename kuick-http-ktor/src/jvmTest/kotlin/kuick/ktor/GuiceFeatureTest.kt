package kuick.ktor

import io.ktor.application.*
import io.ktor.http.*
import io.ktor.response.*
import io.ktor.routing.*
import io.ktor.server.testing.*
import kotlinx.coroutines.*
import kuick.db.*
import kuick.di.*
import kuick.repositories.squash.*
import org.jetbrains.squash.dialects.h2.*
import org.junit.Test
import kotlin.test.*

class GuiceFeatureTest {
    class Hello(val hello: String)

    data class User(
            val userId: Id,
            val firstName: String
    ) {
        data class Id(override val id: String) : kuick.models.Id
    }

    @Test
    fun test() = runBlocking {
        withTestApplication {
            application.install(GuiceFeature) {
                injector {
                    bindToInstance(Hello("world"))
                }
            }
            application.routing {
                get("/hello") {
                    call.respondText("HELLO ${injector().get<Hello>().hello}")
                }
            }
            assertEquals("HELLO world", handleRequest(HttpMethod.Get, "/hello").response.content)
        }
    }

    @Test
    fun testTransaction() = runBlocking {
        withTestApplication {
            val db = H2Connection.createMemoryConnection()
            val repo = ModelRepositorySquash(User::class, User::userId)

            val guice = application.install(GuiceFeature) {
                injector {
                    bindToInstance(db)
                    bindToType<DomainTransactionService, DomainTransactionServiceSquash>()
                }
            }

            guice.withInjector {
                repo.init()
                repo.insert(User(User.Id("hello"), "world"))
            }

            application.routing {
                get("/hello") {
                    val user = repo.findById(User.Id("hello"))
                    call.respondText("${user?.firstName}")
                }
            }
            assertEquals("world", handleRequest(HttpMethod.Get, "/hello").response.content)
        }
    }
}