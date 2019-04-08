package kuick.client.db

import kuick.client.sql.*
import java.io.*
import java.lang.RuntimeException

class DbException(message: String?, cause: Throwable) : RuntimeException(message, cause)

interface DbDriver {
    suspend fun connect(url: String): DbConnection
}

interface DbPreparable {
    val sql: SqlBuilder
    suspend fun prepare(sql: String): DbPreparedStatement
}

suspend fun DbPreparable.query(sql: String, vararg args: Any?): List<DbRow> = prepare(sql).use { it.exec(*args) }

// Tables
suspend fun DbPreparable.createTable(table: String) = query(sql.sqlCreateTable(table))
suspend fun DbPreparable.dropTable(table: String) = query(sql.sqlDropTable(table))

// Columns
suspend fun DbPreparable.addColumn(table: String, column: String, type: String) = query(sql.sqlAddColumn(table, column, type))
suspend fun DbPreparable.dropColumn(table: String, column: String) = query(sql.sqlDropColumn(table, column))

// Indices
fun DbPreparable.createIndexName(columns: List<String>, unique: Boolean = false): String {
    val uniqueStr = if (unique) "unique_" else ""
    return "idx_$uniqueStr${columns.joinToString("_")}"
}
suspend fun DbPreparable.createIndex(table: String, columns: List<String>, unique: Boolean = false, name: String = createIndexName(columns, unique)) = query(sql.sqlCreateIndex(table, columns, unique, name))
suspend fun DbPreparable.dropIndex(table: String, name: String) = query(sql.sqlDropIndex(table, name))


suspend fun DbPreparable.insert(tableName: String, columns: List<String>, vararg values: List<String>) {
    prepare(sql.sqlInsert(tableName, columns)).use { stm ->
        for (value in values) {
            stm.exec(*value.toTypedArray())
        }
    }
}

interface DbConnection : DbPreparable, Closeable {
    override val sql: SqlBuilder
    suspend fun <T> transaction(callback: suspend (DbTransaction) -> T): T
    override suspend fun prepare(sql: String): DbPreparedStatement = transaction { tr -> tr.prepare(sql) }
    override fun close()
}

interface DbTransaction : DbPreparable {
    override val sql: SqlBuilder
    override suspend fun prepare(sql: String): DbPreparedStatement
}

interface DbPreparedStatement : Closeable {
    suspend fun exec(vararg args: Any?): List<DbRow>
    override fun close()
}

data class DbColumns(val names: List<String>) {
    private val namesToPos = names.withIndex().map { it.value to it.index }.toMap()
    val size get() = names.size
    fun get(name: String): Int? = namesToPos[name]
}

data class DbRow(val columns: DbColumns, val values: List<Any?>) {
    val size get() = columns.size
    fun get(name: String): Any? = columns.get(name)?.let { get(it) }
    fun get(index: Int): Any? = values.getOrNull(index)
    override fun toString(): String = columns.names.zip(values).joinToString(",") { "${it.first}: ${it.second}" }

    companion object {
        val RESULT_FALSE = listOf(DbRow(DbColumns(listOf("result")), listOf(false)))
        val RESULT_TRUE = listOf(DbRow(DbColumns(listOf("result")), listOf(true)))
        fun RESULT_BOOL(value: Boolean) = if (value) RESULT_TRUE else RESULT_FALSE
    }
}
