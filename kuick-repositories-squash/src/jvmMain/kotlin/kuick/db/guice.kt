package kuick.db

import com.google.inject.*
import kuick.di.*
import kuick.repositories.squash.*
import org.jetbrains.squash.connection.*

fun Binder.bindDatabaseSquash(db: DatabaseConnection) {
    bindToInstance(db)
    @Suppress("DEPRECATION")
    bindToType<DomainTransactionService, DomainTransactionServiceSquash>()
}
