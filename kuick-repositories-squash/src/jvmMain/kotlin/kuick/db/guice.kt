package kuick.db

import com.google.inject.*
import kuick.di.*
import kuick.repositories.squash.*
import org.jetbrains.squash.connection.*

@Suppress("DEPRECATION")
fun Binder.bindDatabaseSquash(db: DatabaseConnection) {
    bind(db)
    bind<DomainTransactionService, DomainTransactionServiceSquash>()
}
