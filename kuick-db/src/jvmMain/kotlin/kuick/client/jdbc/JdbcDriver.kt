package kuick.client.jdbc

import kotlinx.coroutines.*
import kuick.client.db.*
import kuick.client.sql.*
import org.h2.jdbc.*
import java.sql.*

object JdbcDriver : DbDriver {
    //internal val Dispatchers = kotlinx.coroutines.Dispatchers.IO
    //internal val Dispatchers = kotlinx.coroutines.Dispatchers.Unconfined
    //internal val Dispatchers = kotlinx.coroutines.Dispatchers.Default

    override suspend fun connect(url: String): DbConnection = JdbcConnection(url, DriverManager.getConnection(url))
    suspend fun connectMemoryH2(index: Int = 0) = connect("jdbc:h2:mem:$index")
}

suspend fun DbDriver.connectJdbcMemoryH2(index: Int = 0) = connect("jdbc:h2:mem:$index")

class JdbcConnection(val url: String, val connection: Connection) : DbConnection {
    override val sql = when {
        url.startsWith("jdbc:postgre", ignoreCase = true) -> PgSqlBuilder
        url.startsWith("jdbc:h2", ignoreCase = true) -> H2SqlBuilder
        else -> SqlBuilder.Iso
    }

    override suspend fun <T> transaction(callback: suspend DbTransaction.() -> T): T {
        connection.autoCommit = false
        connection.setSavepoint()
        val result = try {
            callback(JdbcTransaction(this))
        } catch (e: Throwable) {
            connection.rollback()
            throw e
        }
        connection.commit()
        return result
    }

    override fun close() {
        connection.close()
    }
}

class JdbcTransaction(val connection: JdbcConnection) : DbTransaction {
    override val sql: SqlBuilder get() = connection.sql

    override suspend fun <T> prepare(sql: String, callback: suspend (DbPreparedStatement) -> T): T {
        return JdbcPreparedStatement(sql, connection.connection.prepareStatement(sql)).use { callback(it) }
    }
}

class JdbcPreparedStatement(override val sql: String, val prepareStatement: PreparedStatement) : DbPreparedStatement {
    val isSelection = sql.startsWith("SELECT", ignoreCase = true) || sql.startsWith("DESCRIBE", ignoreCase = true) || sql.startsWith("SHOW", ignoreCase = true)
    override suspend fun exec(vararg args: Any?): DbRowSet {
        for (n in 0 until args.size) prepareStatement.setObject(n + 1, args[n])
        try {
            //return withContext(JdbcDriver.Dispatchers) {
            return run {
                if (isSelection) {
                    prepareStatement.executeQuery().toListDbRow()
                } else {
                    prepareStatement.execute()
                    DbRowSet.RESULT_INT(prepareStatement.updateCount)
                }
            }
        } catch (e: JdbcSQLException) {
            throw DbException(e.message, sql, e)
        }
    }

    private fun ResultSet.toListDbRow(): DbRowSet {
        val metadata = this.metaData
        val columnCount = metadata.columnCount
        val columns = DbColumns((1..columnCount).map { metadata.getColumnName(it) })
        val out = arrayListOf<DbRow>()
        while (this.next()) out += DbRow(columns, (1..columnCount).map { this.getObject(it) })
        return DbRowSet(columns, out)
    }

    override fun close() {
        prepareStatement.close()
    }
}
