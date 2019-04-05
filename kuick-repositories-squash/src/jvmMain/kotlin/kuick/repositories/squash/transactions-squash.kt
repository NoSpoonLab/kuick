package kuick.repositories.squash

import kotlinx.coroutines.withContext
import kuick.core.*
import kuick.db.*
import kuick.repositories.squash.orm.*
import org.jetbrains.squash.connection.DatabaseConnection
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.coroutines.coroutineContext


@Singleton
class DomainTransactionServiceSquash @Inject constructor(val db: DatabaseConnection): DomainTransactionService {
    override suspend fun createNewConnection(callback: suspend () -> Unit) {
        withContext(DiscardDomainTransactionContext) {
            callback()
        }
    }

    @Suppress("OverridingDeprecatedMember")
    @UseExperimental(KuickInternalWarning::class)
    override suspend fun <T : Any> transactionNullable(transactionalActions: suspend (DomainTransaction) -> T?): T? {

        //println("LazyDomainTransactionSquash($db)")
        val ctx = coroutineContext[DomainTransactionContext.Key]
        return if (ctx != null && ctx != DiscardDomainTransactionContext) {
            //println("Reentrando en transaction {}")
            domainTransaction { tr -> transactionalActions(tr) }
        } else {
            LazyDomainTransactionSquash(db).use { domainTransaction ->
                withContext(NormalDomainTransactionContext(domainTransaction)) {
                    transactionalActions(domainTransaction)
                }
            }
        }
    }

}