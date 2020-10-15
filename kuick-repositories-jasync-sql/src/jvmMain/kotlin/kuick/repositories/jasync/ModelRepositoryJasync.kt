package kuick.repositories.jasync

import com.github.jasync.sql.db.QueryResult
import kuick.repositories.ModelQuery
import kuick.repositories.ModelRepository
import kotlin.reflect.KClass
import kotlin.reflect.KProperty1

open class ModelRepositoryJasync<I : Any, T : Any>(
    val modelClass: KClass<T>,
    val tableName: String,
    override val idField: KProperty1<T, I>,
    protected val pool: JasyncPool
) : ModelRepository<I, T> {

    private val mqb = ModelSqlBuilder(modelClass, tableName)

    private suspend fun ModelSqlBuilder.PreparedSql.execute(): QueryResult =
        pool.prepQuery(sql, values)


    override suspend fun insert(t: T): T {
        mqb.insertPreparedSql(t).execute()
        return t
    }

    override suspend fun insertMany(ts: Collection<T>) {
        pool.query(mqb.insertManySql(ts))
    }

    override suspend fun updateBy(t: T, q: ModelQuery<T>): T {
        mqb.updatePreparedSql(t, q).execute()
        return t
    }

    override suspend fun deleteBy(q: ModelQuery<T>) {
        mqb.deletePreparedSql(q).execute()
    }

    override suspend fun init() {
        // TODO ¿DML de creación de la tabla?
    }

    override suspend fun findBy(q: ModelQuery<T>): List<T> = mqb
        .selectPreparedSql(q)
        .execute()
        .rows
        .map {  row ->
            mqb.modelFromValues( (0 until row.size).map { i -> row[i] } )
        }

    override suspend fun getAll(): List<T> {
        throw NotImplementedError()
    }

}
