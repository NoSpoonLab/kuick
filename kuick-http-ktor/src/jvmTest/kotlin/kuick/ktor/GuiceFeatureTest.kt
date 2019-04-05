package kuick.ktor

import io.ktor.application.*
import io.ktor.http.*
import io.ktor.response.*
import io.ktor.routing.*
import io.ktor.server.testing.*
import kotlinx.coroutines.*
import kuick.core.*
import kuick.db.*
import kuick.di.*
import kuick.repositories.squash.*
import org.jetbrains.squash.dialects.h2.*
import org.junit.Test
import kotlin.test.*

@UseExperimental(KuickInternal::class)
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
                    bind(Hello("world"))
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
                    bindDatabaseSquash(db)
                }
            }

            guice.withInjector {
                repo.init()
                repo.insert(User(User.Id("hello"), "world"))
            }

            val transactionServices = arrayListOf<DomainTransactionService>()

            application.routing {
                get("/hello") {
                    transactionServices += injector().get<DomainTransactionService>()
                    val user = repo.findById(User.Id("hello"))
                    call.respondText("${user?.firstName}")
                }
            }
            assertEquals("world", handleRequest(HttpMethod.Get, "/hello").response.content)
            assertEquals("world", handleRequest(HttpMethod.Get, "/hello").response.content)
            assertEquals(2, transactionServices.size)
            assertSame(transactionServices[0], transactionServices[1])
        }
    }

    @Test
    fun testPerRequestDecorator() = runBlocking {
        withTestApplication {
            val db = H2Connection.createMemoryConnection()
            val repo = ModelRepositorySquash(User::class, User::userId)

            val guice = application.install(GuiceFeature) {
                injector {
                    bindDatabaseSquashNoDomainTransaction(db)
                }
                perRequestInjectorDecorator { injector ->
                    injector.createChildInjector(GuiceModule {
                        bindDomainTransactionSquash(injector.get())
                    })
                }
            }

            guice.withInjector {
                repo.init()
                repo.insert(User(User.Id("hello"), "world"))
            }

            val transactionServices = arrayListOf<DomainTransactionService>()

            application.routing {
                get("/hello") {
                    transactionServices += injector().get<DomainTransactionService>()
                    println("transactionServices: " + transactionServices)
                    val user = repo.findById(User.Id("hello"))
                    call.respondText("${user?.firstName}")
                }
            }
            assertEquals("world", handleRequest(HttpMethod.Get, "/hello").response.content)
            assertEquals("world", handleRequest(HttpMethod.Get, "/hello").response.content)

            assertEquals(2, transactionServices.size)
            assertNotSame(transactionServices[0], transactionServices[1])
        }
    }
}