package kuick.client.repositories

import kuick.client.db.*
import kuick.client.orm.*
import kuick.orm.*
import kuick.repositories.*
import kotlin.reflect.*

class DbModelRepository<I : Any, T : Any>(
        val table: TableDefinition<T>,
        override val idField: KProperty1<T, I>
) : ModelRepository<I, T> {
    constructor(clazz: KClass<T>, idField: KProperty1<T, I>, serializationStrategy: TableSerializationStrategy = defaultTableSerializationStrategy) : this(TableDefinition(clazz, serializationStrategy), idField)

    override suspend fun init() = dbClient { it.synchronizeTable(table) }
    override suspend fun insert(t: T): T = dbClient { it.insert(table, t) }

    override suspend fun updateBy(t: T, q: ModelQuery<T>): T {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override suspend fun deleteBy(q: ModelQuery<T>): Unit = run { dbClient { it.query(it.sql.sqlDelete(q, table)) } }
    override suspend fun findBy(q: ModelQuery<T>): List<T> = dbClient { it.query(table, it.sql.sqlSelect(q, table)) }
    override suspend fun getAll(): List<T> = dbClient { it.query(table, "SELECT * FROM ${it.sql.quoteTableName(table.name)};") }
}
