package kuick.db

import kuick.db.DomainTransaction

interface RequiresSchema {

    fun createSchema(tr: DomainTransaction)

}