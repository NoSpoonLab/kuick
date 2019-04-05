package kuick.repositories.squash

import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withContext
import kuick.core.*
import kuick.db.DomainTransaction
import kuick.db.DomainTransactionContext
import kuick.db.DomainTransactionService
import kuick.db.domainTransaction
import kuick.repositories.squash.orm.*
import org.jetbrains.squash.connection.DatabaseConnection
import org.jetbrains.squash.connection.transaction
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.coroutines.coroutineContext


@Singleton
class DomainTransactionServiceSquash @Inject constructor(val db: DatabaseConnection): DomainTransactionService {

    @Suppress("OverridingDeprecatedMember")
    @UseExperimental(KuickInternalWarning::class)
    override suspend fun <T : Any> transactionNullable(transactionalActions: suspend (DomainTransaction) -> T?): T? {

        //println("LazyDomainTransactionSquash($db)")
        return if (coroutineContext[DomainTransactionContext.Key] != null) {
            //println("Reentrando en transaction {}")
            domainTransaction { tr -> transactionalActions(tr) }
        } else {
            LazyDomainTransactionSquash(db).use { domainTransaction ->
                withContext(DomainTransactionContext(domainTransaction)) {
                    transactionalActions(domainTransaction)
                }
            }
        }
    }

}