package kuick.client.db

import java.io.*

interface DbDriver {
    suspend fun connect(url: String): DbConnection
}

interface DbConnection : Closeable {
    suspend fun <T> transaction(callback: suspend (DbTransaction) -> T): T
    override fun close()
}

interface DbTransaction {
    suspend fun prepare(sql: String): DbPreparedStatement
    suspend fun query(sql: String, vararg args: Any?): List<DbRow> = prepare(sql).use { it.exec(*args) }
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
}
