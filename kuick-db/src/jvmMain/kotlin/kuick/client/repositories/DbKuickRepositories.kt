package kuick.client.repositories

import kuick.client.db.*
import kuick.client.orm.*
import kuick.orm.*
import kuick.repositories.*
import kotlin.reflect.*

inline fun <reified T : Any, I : Any> DbModelRepository(idField: KProperty1<T, I>): ModelRepository<I, T> = DbModelRepository(T::class, idField)

open class DbModelRepository<I : Any, T : Any>(
        val table: TableDefinition<T>,
        override val idField: KProperty1<T, I>
) : ModelRepository<I, T> {
    constructor(clazz: KClass<T>, idField: KProperty1<T, I>, serializationStrategy: TableSerializationStrategy = defaultTableSerializationStrategy) : this(TableDefinition(clazz, serializationStrategy), idField)

    override suspend fun init() = dbClient { it.synchronizeTable(table) }
    override suspend fun insert(t: T): T = dbClient { it.insert(table, t) }

    override suspend fun updateBy(t: T, q: ModelQuery<T>): T {
        val map = table.untype(t)
        dbClient {
            val query = it.sql.sqlUpdate(map.keys.toList(), q, table)
            it.query(query.sql, *map.values.toTypedArray(), *query.params.toTypedArray())
        }
        return t
    }

    override suspend fun update(
        set: Map<KProperty1<T, *>, Any>,
        incr: Map<KProperty1<T, Number>, Number>,
        where: ModelQuery<T>
    ): Int {
        val result = dbClient {
            val query = it.sql.sqlUpdateIncr(set.keys.map { table[it].name }, incr.keys.map { table[it].name }, where, table)
            it.query(query.sql, *(set.values + incr.values).toTypedArray(), *query.params.toTypedArray())
        }
        return result.first().first().toString().toIntOrNull() ?: -1
    }

    override suspend fun deleteBy(q: ModelQuery<T>): Unit = run { dbClient { it.query(it.sql.sqlDelete(q, table, it.sql.outParams())) } }
    override suspend fun findBy(q: ModelQuery<T>): List<T> = dbClient { it.query(table, it.sql.sqlSelect(q, table)) }
    override suspend fun getAll(): List<T> = dbClient { it.query(table, "SELECT * FROM ${it.sql.quoteTableName(table.name)};") }
}
