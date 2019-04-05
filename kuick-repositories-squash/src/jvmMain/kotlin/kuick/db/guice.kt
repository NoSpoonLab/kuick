package kuick.db

import com.google.inject.*
import kuick.di.*
import kuick.repositories.squash.*
import org.jetbrains.squash.connection.*

@Suppress("DEPRECATION")
fun Binder.bindDatabaseSquash(db: DatabaseConnection) {
    bindDatabaseSquashNoDomainTransaction(db)
    bindDomainTransactionSquash(db)
}

fun Binder.bindDatabaseSquashNoDomainTransaction(db: DatabaseConnection) {
    bind(db)
}

fun Binder.bindDomainTransactionSquash(db: DatabaseConnection) {
    bind<DomainTransactionService>(DomainTransactionServiceSquash(db))
}

fun Injector.registerSquashPerCoroutineJob() {
    val perCoroutineJob = get<PerCoroutineJob>()
    val domainTransactionService = get<DomainTransactionService>()
    get<PerCoroutineJob>().register { callback ->
        domainTransactionService.createNewConnection {
            callback()
        }
    }
}