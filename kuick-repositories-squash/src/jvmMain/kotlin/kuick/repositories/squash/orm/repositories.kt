package kuick.repositories.squash.orm

import kuick.db.DomainTransaction
import kuick.db.RequiresSchema
import kuick.models.Id
import org.jetbrains.squash.expressions.Expression
import org.jetbrains.squash.schema.create


@Deprecated("")
interface EntityRepository<I: Id, T: Any> {

    suspend fun insert(tr: DomainTransaction, t: T): T

    suspend fun update(tr: DomainTransaction, t: T): T

    suspend fun upsert(tr: DomainTransaction, t: T): T

    suspend fun detete(tr: DomainTransaction, i: I)

    suspend fun getById(tr: DomainTransaction, i: I): T?

}


@Deprecated("")
open class BaseSquashRepository<I: Id, T: Any>(
        val schema: ORMTableDefinition<T>,
        val getId: (T) -> I,
        val whereById: (I) -> Expression<Boolean>): EntityRepository<I, T>, RequiresSchema {

    override fun createSchema(tr: DomainTransaction) {
        tr.squashTr().databaseSchema().create(schema)
    }

    override suspend fun insert(tr: DomainTransaction, t: T): T = schema.insert(tr, t)

    override suspend fun update(tr: DomainTransaction, t: T): T = schema.update(tr, t) { whereById(getId(t)) }

    override suspend fun detete(tr: DomainTransaction, i: I) = schema.delete(tr) { whereById(i) }

    override suspend fun getById(tr: DomainTransaction, i: I): T? = schema.selectOne(tr) { whereById(i) }

    override suspend fun upsert(tr: DomainTransaction, t: T): T {
        val entity = getById(tr, getId(t))
        return when (entity) {
            null -> insert(tr, t)
            else -> update(tr, t)
        }
    }
}






@Deprecated("")
interface EntityTwoKeysRepository<I1: Id, I2: Any, T: Any> {

    suspend fun insert(tr: DomainTransaction, t: T): T

    suspend fun update(tr: DomainTransaction, t: T): T

    suspend fun detete(tr: DomainTransaction, i1: I1, i2: I2)

    suspend fun getById(tr: DomainTransaction, i1: I1, i2: I2): T?

}


@Deprecated("")
open class BaseSquashTwoKeysRepository<I1: Id, I2: Any, T: Any>(
        val schema: ORMTableDefinition<T>,
        val getId: (T) -> Pair<I1, I2>,
        val whereById: (I1, I2) -> Expression<Boolean>):
        EntityTwoKeysRepository<I1, I2, T>,
        RequiresSchema {

    override fun createSchema(tr: DomainTransaction) {
        tr.squashTr().databaseSchema().create(schema)
    }

    override suspend fun insert(tr: DomainTransaction, t: T): T = schema.insert(tr, t)

    override suspend fun update(tr: DomainTransaction, t: T): T = schema.update(tr, t) {
        val (i1, i2) = getId(t)
        whereById(i1, i2)
    }

    override suspend fun detete(tr: DomainTransaction, i1: I1, i2: I2) = schema.delete(tr) { whereById(i1, i2) }

    override suspend fun getById(tr: DomainTransaction, i1: I1, i2: I2): T? = schema.selectOne(tr) { whereById(i1, i2) }

}




@Deprecated("")
data class ThreeKeys<I1: Id, I2: Id, I3: Id>(val k1: I1, val k2: I2, val k3: I3)

@Deprecated("")
open class BaseSquashThreeKeysDAL<I1: Id, I2: Id, I3: Id, T: Any>(
        val schema: ORMTableDefinition<T>,
        val getId: (T) -> ThreeKeys<I1, I2, I3>,
        val whereById: (I1, I2, I3) -> Expression<Boolean>): RequiresSchema {

    override fun createSchema(tr: DomainTransaction) {
        tr.squashTr().databaseSchema().create(schema)
    }

    suspend fun insert(tr: DomainTransaction, t: T): T = schema.insert(tr, t)

    suspend fun update(tr: DomainTransaction, t: T): T = schema.update(tr, t) {
        val (i1, i2, i3) = getId(t)
        whereById(i1, i2, i3)
    }

    suspend fun detete(tr: DomainTransaction, i1: I1, i2: I2, i3: I3) = schema.delete(tr) { whereById(i1, i2, i3) }

    suspend fun getById(tr: DomainTransaction, i1: I1, i2: I2, i3: I3): T? = schema.selectOne(tr) { whereById(i1, i2, i3) }

}