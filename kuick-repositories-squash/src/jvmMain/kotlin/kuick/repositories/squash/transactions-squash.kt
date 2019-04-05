package kuick.repositories.squash

import kotlinx.coroutines.*
import kuick.core.*
import kuick.db.*
import kuick.di.*
import kuick.repositories.squash.orm.*
import org.jetbrains.squash.connection.*
import javax.inject.*
import kotlin.coroutines.*


@Singleton
class DomainTransactionServiceSquash @Inject constructor(val db: DatabaseConnection, val perCoroutineJob: PerCoroutineJob) : DomainTransactionService {
    init {
        perCoroutineJob.register { callback ->
            this@DomainTransactionServiceSquash.createNewConnection {
                callback()
            }
        }
    }

    override suspend fun createNewConnection(callback: suspend () -> Unit) {
        withContext(DiscardDomainTransactionContext) {
            callback()
        }
    }

    @Suppress("OverridingDeprecatedMember")
    @UseExperimental(KuickInternalWarning::class)
    override suspend fun <T : Any> transactionNullable(transactionalActions: suspend (DomainTransaction) -> T?): T? {

        //println("LazyDomainTransactionSquash($db)")
        val ctx = coroutineContext[BaseDomainTransactionContext.Key]
        return if (ctx != null && ctx != DiscardDomainTransactionContext) {
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