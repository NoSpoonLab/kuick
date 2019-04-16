package kuick.client.db

import kuick.client.sql.*
import java.io.*
import java.lang.RuntimeException
import kotlin.coroutines.*

class DbException(message: String?, val sql: String, cause: Throwable) : RuntimeException("$message in $sql", cause)

interface DbDriver {
    suspend fun connect(url: String): DbConnection
}

interface DbPreparable {
    val sql: SqlBuilder
    suspend fun <T> prepare(sql: String, callback: suspend (DbPreparedStatement) -> T): T
}

data class QueryAndParams(val sql: String, val params: List<Any?>)
suspend fun DbPreparable.query(query: QueryAndParams): DbRowSet = query(query.sql, *query.params.toTypedArray())
suspend fun DbPreparable.query(sql: String, vararg args: Any?): DbRowSet = prepare(sql) { it.exec(*args) }
suspend fun DbPreparable.delete(table: String, condition: String): DbRowSet = query(sql.sqlDelete(table, condition))
suspend fun DbPreparable.deleteAll(table: String): DbRowSet = delete(table, "1=1")

// Tables
suspend fun DbPreparable.createTable(table: String, ifNotExists: Boolean = true) = query(sql.sqlCreateTable(table, ifNotExists))
suspend fun DbPreparable.dropTable(table: String, ifExists: Boolean = true) = query(sql.sqlDropTable(table, ifExists))
suspend fun DbPreparable.listTables() = query(sql.sqlListTables()).map { it.first() as String }

// Columns
suspend fun DbPreparable.addColumn(table: String, column: String, type: String, nullable: Boolean = true) = query(sql.sqlAddColumn(table, column, type, nullable))
suspend fun DbPreparable.dropColumn(table: String, column: String) = query(sql.sqlDropColumn(table, column))
suspend fun DbPreparable.listColumns(table: String) = query(sql.sqlListColumns(table)).map { it.first() as String }

// Indices
fun DbPreparable.createIndexName(columns: List<String>, unique: Boolean = false): String {
    val uniqueStr = if (unique) "unique_" else ""
    return "idx_$uniqueStr${columns.joinToString("_")}"
}
suspend fun DbPreparable.createIndex(table: String, columns: List<String>, unique: Boolean = false, name: String = createIndexName(columns, unique)) = query(sql.sqlCreateIndex(table, columns, unique, name))
suspend fun DbPreparable.dropIndex(table: String, name: String) = query(sql.sqlDropIndex(table, name))


suspend fun DbPreparable.insert(tableName: String, columns: List<String>, vararg values: List<Any?>) {
    prepare(sql.sqlInsert(tableName, columns)) { stm ->
        for (value in values) {
            stm.exec(*value.toTypedArray())
        }
    }
}

suspend fun DbPreparable.insert(tableName: String, data: Map<String, Any?>) = insert(tableName, data.keys.toList(), data.values.toList())

interface DbConnectionProvider : CoroutineContext.Element {
    override val key get() = DbConnectionProvider

    suspend fun <T> get(callback: suspend (DbConnection) -> T): T
    companion object : CoroutineContext.Key<DbConnectionProvider>
}

fun DbConnectionProvider(autoClose: Boolean = false, provider: suspend () -> DbConnection) = object : DbConnectionProvider {
    override suspend fun <T> get(callback: suspend (DbConnection) -> T): T {
        val connection = provider()
        try {
            return callback(connection)
        } finally {
            if (autoClose) connection.close()
        }
    }
}

suspend fun dbConnectionProvider(): DbConnectionProvider = coroutineContext[DbConnectionProvider] ?: error("Not DbConnectionProvider in the coroutineContext")
suspend fun <T> dbClient(callback: suspend (DbConnection) -> T) = dbConnectionProvider().get { callback(it) }

suspend fun <T> DbConnectionProvider.getTransaction(callback: suspend (DbTransaction) -> T): T {
    return get { connection ->
        connection.transaction {
            callback(it)
        }
    }
}

interface DbConnection : DbPreparable, Closeable {
    override val sql: SqlBuilder
    suspend fun <T> transaction(callback: suspend (DbTransaction) -> T): T
    override suspend fun <T> prepare(sql: String, callback: suspend (DbPreparedStatement) -> T): T = transaction { tr -> tr.prepare(sql, callback) }
    //override suspend fun prepare(sql: String): DbPreparedStatement = transaction { tr -> tr.prepare(sql) }
    override fun close()
}

interface DbTransaction : DbPreparable {
    override val sql: SqlBuilder
}

interface DbPreparedStatement : Closeable {
    val sql: String
    suspend fun exec(vararg args: Any?): DbRowSet
    override fun close()
}

class DbRowSet(val columns: DbColumns, val rows: List<DbRow>) : List<DbRow> by rows {
    companion object {
        private val RESULT_COLUMNS = DbColumns(listOf("result"))
        val RESULT_FALSE = DbRowSet(RESULT_COLUMNS, listOf(DbRow(RESULT_COLUMNS, listOf(false))))
        val RESULT_TRUE = DbRowSet(RESULT_COLUMNS, listOf(DbRow(RESULT_COLUMNS, listOf(true))))
        fun RESULT_BOOL(value: Boolean) = if (value) RESULT_TRUE else RESULT_FALSE

        operator fun invoke(vararg rows: Map<String, Any?>): DbRowSet = invoke(rows.toList())

        operator fun invoke(rows: List<Map<String, Any?>>): DbRowSet {
            val keys = rows.map { it.keys }.reduce { a, b -> a + b }
            val columns = DbColumns(keys.toList())
            return DbRowSet(columns, rows.map { row -> DbRow(columns, columns.map { row[it] }) })
        }
    }
    operator fun get(row: Int, column: Int): Any? = this[row][column]
    operator fun get(row: Int, column: String): Any? = this[row][column]
    override fun toString(): String = rows.toString()
}

data class DbColumns(val names: List<String>) : List<String> by names {
    constructor(vararg names: String) : this(names.toList())
    private val namesToPos by lazy { names.withIndex().map { it.value to it.index }.toMap() }
    override val size get() = names.size
    fun get(name: String): Int? = namesToPos[name]
}

data class DbRow(val columns: DbColumns, val values: List<Any?>) : List<Any?> by values {
    override val size get() = columns.size
    val map by lazy { columns.zip(values).map { it.first to it.second }.toMap() }
    operator fun get(name: String): Any? = columns.get(name)?.let { get(it) }
    override operator fun get(index: Int): Any? = values.getOrNull(index)
    override fun toString(): String = map.toString()
}
