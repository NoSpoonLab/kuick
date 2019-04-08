package kuick.client.jdbc

import kotlinx.coroutines.*
import kuick.client.db.*
import kuick.client.sql.*
import org.h2.jdbc.*
import java.sql.*

object JdbcDriver : DbDriver {
    internal val Dispatchers = kotlinx.coroutines.Dispatchers.IO

    override suspend fun connect(url: String): DbConnection = JdbcConnection(DriverManager.getConnection(url))
}

class JdbcConnection(val connection: Connection) : DbConnection {
    override val sql = SqlBuilder.Iso

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
    override suspend fun prepare(sql: String) = JdbcPreparedStatement(sql, connection.connection.prepareStatement(sql))
}

class JdbcPreparedStatement(val sql: String, val prepareStatement: PreparedStatement) : DbPreparedStatement {
    val isSelection = sql.startsWith("SELECT", ignoreCase = true)
    override suspend fun exec(vararg args: Any?): List<DbRow> {
        for (n in 0 until args.size) prepareStatement.setObject(n + 1, args[n])
        try {
            return withContext(JdbcDriver.Dispatchers) {
                if (isSelection) {
                    prepareStatement.executeQuery().toListDbRow()
                } else {
                    DbRow.RESULT_BOOL(prepareStatement.execute())
                }
            }
        } catch (e: JdbcSQLException) {
            throw DbException(e.message, e)
        }
    }

    private fun ResultSet.toListDbRow(): List<DbRow> {
        val metadata = this.metaData
        val columnCount = metadata.columnCount
        val columns = DbColumns((1..columnCount).map { metadata.getColumnName(it) })
        val out = arrayListOf<DbRow>()
        while (this.next()) out += DbRow(columns, (1..columnCount).map { this.getObject(it) })
        return out
    }

    override fun close() {
        prepareStatement.close()
    }
}
