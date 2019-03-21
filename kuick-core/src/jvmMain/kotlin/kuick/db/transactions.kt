package kuick.db

import kotlinx.coroutines.*
import kotlin.coroutines.AbstractCoroutineContextElement
import kotlin.coroutines.CoroutineContext
import kotlin.coroutines.coroutineContext



interface DomainTransaction


interface DomainTransactionService {

    suspend operator fun <T> invoke(transactionalActions: suspend (DomainTransaction) -> T): T {
        @Suppress("DEPRECATION")
        return transactionNullable<Any>(transactionalActions as (suspend (DomainTransaction) -> T?)) as T
    }

    @Deprecated("", ReplaceWith("invoke(transactionalActions)"))
    suspend fun <T : Any> transaction(transactionalActions: suspend (DomainTransaction) -> T): T = invoke(transactionalActions)

    @Deprecated("", ReplaceWith("invoke(transactionalActions)"))
    suspend fun <T:Any> transactionNullable(transactionalActions: suspend (DomainTransaction) -> T?): T?

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
class DomainTransactionContext(val tr: DomainTransaction) : AbstractCoroutineContextElement(Key) {
    companion object Key : CoroutineContext.Key<DomainTransactionContext>
}

class NotInTransactionException: RuntimeException()

suspend fun domainTransaction(): DomainTransaction = coroutineContext[DomainTransactionContext]?.tr ?: throw NotInTransactionException()