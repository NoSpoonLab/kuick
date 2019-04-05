package kuick.repositories.squash

import com.google.inject.*
import kotlinx.coroutines.*
import kuick.db.*
import org.jetbrains.squash.connection.*
import org.jetbrains.squash.dialects.h2.*
import kotlin.reflect.*

class InfrastructureGuiceModule() : AbstractModule() {
    override fun configure() {
        val db: DatabaseConnection = H2Connection.createMemoryConnection()
        bind(DatabaseConnection::class.java).toInstance(db)
        bind(DomainTransactionService::class.java).to(DomainTransactionServiceSquash::class.java)
    }
}

abstract class AbstractITTest(modules: List<Module> = listOf(InfrastructureGuiceModule())) {

    constructor(vararg modules: AbstractModule) : this(modules.toList())


    val injector = Guice.createInjector(modules)
    val trService = injector.getInstance(DomainTransactionService::class.java)
    fun <T : Any> instance(kClass: KClass<T>): T = injector.getInstance(kClass.java)

    fun testInTransaction(actions: suspend () -> Unit) = runBlocking {
        trService.transaction { tr ->
            withContext(DomainTransactionContext(tr)) {
                actions()
            }
        }
    }


}