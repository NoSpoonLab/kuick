package kuick.repositories.squash.orm

import kuick.db.DomainTransaction
import kuick.db.RequiresSchema
import kuick.db.domainTransaction
import kuick.models.Id
import org.jetbrains.squash.expressions.Expression
import org.jetbrains.squash.schema.create


interface ContextualRepository<I: Id, T: Any> {

    suspend fun insert(t: T): T

    suspend fun update(t: T): T

    suspend fun upsert(t: T): T

    suspend fun detete(i: I)

    suspend fun getById(i: I): T?

}


open class SquashContextualRepository<I: Id, T: Any>(
        val schema: ORMTableDefinition<T>,
        val getId: (T) -> I,
        val whereById: (I) -> Expression<Boolean>): ContextualRepository<I, T>, RequiresSchema {

    override fun createSchema(tr: DomainTransaction) {
        tr.squashTr().databaseSchema().create(schema)
    }

    override suspend fun insert(t: T): T = schema.insert(domainTransaction(), t)

    override suspend fun update(t: T): T = schema.update(domainTransaction(), t) { whereById(getId(t)) }

    override suspend fun detete(i: I) = schema.delete(domainTransaction()) { whereById(i) }

    override suspend fun getById(i: I): T? = schema.selectOne(domainTransaction()) { whereById(i) }

    override suspend fun upsert(t: T): T {
        val entity = getById(getId(t))
        return when (entity) {
            null -> insert(t)
            else -> update(t)
        }
    }
}