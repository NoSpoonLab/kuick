package kuick.db

@Deprecated("")
interface RequiresSchema {

    fun createSchema(tr: DomainTransaction)

}