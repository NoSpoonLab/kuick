package kuick.db

import kotlinx.coroutines.*
import kuick.core.*
import kuick.di.*
import kotlin.coroutines.*


@Deprecated("")
interface DomainTransaction

@Deprecated("")
interface DomainTransactionService {

    suspend operator fun <T> invoke(transactionalActions: suspend (DomainTransaction) -> T): T {
        @Suppress("DEPRECATION")
        return transactionNullable<Any>(transactionalActions as (suspend (DomainTransaction) -> T?)) as T
    }

    @Deprecated("", ReplaceWith("invoke(transactionalActions)"))
    suspend fun <T : Any> transaction(transactionalActions: suspend (DomainTransaction) -> T): T = invoke(transactionalActions)

    @Deprecated("", ReplaceWith("invoke(transactionalActions)"))
    suspend fun <T:Any> transactionNullable(transactionalActions: suspend (DomainTransaction) -> T?): T?

    suspend fun createNewConnection(callback: suspend () -> Unit)

    @Deprecated("")
    fun <T : Any> transactionSync(transactionalActions: (DomainTransaction) -> T): T = runBlocking {
        invoke { transactionalActions(it) }
    }
}

@Deprecated("", level = DeprecationLevel.ERROR)
suspend operator fun <T> DomainTransactionService.invoke(transactionalActions: suspend (DomainTransaction) -> T): T {
    return transactionNullable<Any>(transactionalActions as (suspend (DomainTransaction) -> T?)) as T
}


// coroutine context element that keeps a (mutable) integer counter
abstract class BaseDomainTransactionContext() : AbstractCoroutineContextElement(Key) {
    abstract val tr: DomainTransaction
    companion object Key : CoroutineContext.Key<BaseDomainTransactionContext>
}

class DomainTransactionContext(override val tr: DomainTransaction) : BaseDomainTransactionContext() {
}

object DiscardDomainTransactionContext : BaseDomainTransactionContext() {
    override val tr: DomainTransaction get() = TODO()
}

class NotInTransactionException: RuntimeException()

@KuickInternalWarning
@Deprecated("Do not use domainTransactionOrNull")
suspend fun domainTransactionOrNull(): DomainTransaction? = coroutineContext[BaseDomainTransactionContext]?.takeUnless { it is DiscardDomainTransactionContext }?.tr

@KuickInternalWarning
@Deprecated("Do not use domainTransaction")
suspend fun domainTransaction(): DomainTransaction = domainTransactionOrNull()
        ?: throw NotInTransactionException()

@KuickInternalWarning
@UseExperimental(KuickInternal::class)
@Suppress("DEPRECATION")
suspend fun <T> domainTransaction(block: suspend (DomainTransaction) -> T): T {
    val transaction = domainTransactionOrNull()
    return when {
        transaction != null -> {
            block(transaction)
        }
        else -> {
            val service = injector().get<DomainTransactionService>()
            service { block(it) }
        }
    }
}
