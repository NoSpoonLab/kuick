package kuick.repositories.squash

import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withContext
import kuick.db.DomainTransaction
import kuick.db.DomainTransactionContext
import kuick.db.DomainTransactionService
import kuick.db.domainTransaction
import kuick.repositories.squash.orm.DomainTransactionSquash
import org.jetbrains.squash.connection.DatabaseConnection
import org.jetbrains.squash.connection.transaction
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.coroutines.coroutineContext



private class NullResultInTransactionException: Exception()

@Singleton
class DomainTransactionServiceSquash @Inject constructor(val db: DatabaseConnection): DomainTransactionService {

    suspend override fun <T : Any> transaction(transactionalActions: suspend (DomainTransaction) -> T): T =
            transactionNullable(transactionalActions)!!

    suspend override fun <T : Any> transactionNullable(transactionalActions: suspend (DomainTransaction) -> T?): T? {

        if (coroutineContext[DomainTransactionContext.Key] != null) {
            println("Reentrando en transaction {}")
            return transactionalActions(domainTransaction())
        } else {
            val preTrCtx = coroutineContext
            return db.transaction {
                val domainTransaction = DomainTransactionSquash(this)
                try {
                    val result = runBlocking(preTrCtx) {
                        withContext(DomainTransactionContext(domainTransaction)) {
                            transactionalActions(domainTransaction)
                        }
                    }
                    // TODO Pendiente resolver autoCommit=false
                    //this.commit()
                    result
                } catch (t: Throwable) {
                    //this.rollback()
                    throw t
                }
            }
        }

    }

    override fun <T : Any> transactionSync(transactionalActions: (DomainTransaction) -> T): T =
            db.transaction { transactionalActions(DomainTransactionSquash(this)) }

}