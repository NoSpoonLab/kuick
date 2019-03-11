package kuick.db

import kotlin.coroutines.AbstractCoroutineContextElement
import kotlin.coroutines.CoroutineContext
import kotlin.coroutines.coroutineContext



interface DomainTransaction


interface DomainTransactionService {

    suspend fun <T:Any> transaction(transactionalActions: suspend (DomainTransaction) -> T): T

    suspend fun <T:Any> transactionNullable(transactionalActions: suspend (DomainTransaction) -> T?): T?

    fun <T:Any> transactionSync(transactionalActions: (DomainTransaction) -> T): T
}

suspend operator fun <T> DomainTransactionService.invoke(transactionalActions: suspend (DomainTransaction) -> T): T {
    return transactionNullable<Any>(transactionalActions as (suspend (DomainTransaction) -> T?)) as T
}


// coroutine context element that keeps a (mutable) integer counter
class DomainTransactionContext(val tr: DomainTransaction) : AbstractCoroutineContextElement(Key) {
    companion object Key : CoroutineContext.Key<DomainTransactionContext>
}

class NotInTransactionException: RuntimeException()

suspend fun domainTransaction(): DomainTransaction = coroutineContext[DomainTransactionContext]?.tr ?: throw NotInTransactionException()