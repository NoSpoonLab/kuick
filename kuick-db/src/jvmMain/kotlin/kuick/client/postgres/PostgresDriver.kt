package kuick.client.postgres

import io.reactiverse.kotlin.pgclient.*
import io.reactiverse.kotlin.pgclient.PgClient
import io.reactiverse.pgclient.*
import io.vertx.core.*
import kuick.client.db.*
import kuick.client.sql.*

object PostgresDriver : DbDriver {
    override suspend fun connect(url: String): DbConnection {
        val vertx = Vertx.vertx()
        return PostgressConnection(vertx, PgClient.connect(vertx, url))
    }
}

class PostgressConnection(val closeVertx: Vertx?, val connection: PgConnection) : DbConnection {
    override val sql = PgSqlBuilder

    override suspend fun <T> transaction(callback: suspend (DbTransaction) -> T): T {
        val transaction = connection.begin()
        //println("Started $transaction")
        val result = try {
            callback(PostgressTransaction(this, transaction))
        } catch (e: Throwable) {
            //println("Rollback $transaction")
            kotlin.runCatching { transaction.rollbackAwait() }
            throw e
        }

        //println("Commit $transaction")
        transaction.commitAwait()
        return result
    }

    override fun close() {
        //println("PostgressConnection.close")
        closeVertx?.close()
        connection.close()
    }
}

class PostgressTransaction(val connection: PostgressConnection, val transaction: PgTransaction) : DbTransaction {
    override val sql: SqlBuilder get() = connection.sql
    override suspend fun prepare(sql: String): DbPreparedStatement {
        //println("PostgressTransaction.prepare: $sql, $transaction")
        try {
            return PostgresPreparedStatement(this, sql, transaction.prepareAwait(sql))
        } catch (e: PgException) {
            throw DbException(e.message, sql, e)
        }
    }
}

class PostgresPreparedStatement(val transaction: PostgressTransaction, override val sql: String, val prepared: PgPreparedQuery) : DbPreparedStatement {
    override suspend fun exec(vararg args: Any?): DbRowSet {
        //println("PostgresPreparedStatement.exec: ${transaction}")
        val tuple = when (args.size) {
            0 -> Tuple.tuple()
            1 -> Tuple.of(args.first())
            else -> Tuple.of(args.first(), *args.drop(1).toTypedArray())
        }
        try {
            return prepared.executeAwait(tuple).toListDbRow()
        } catch (e: PgException) {
            throw DbException(e.message, sql, e)
        }
    }

    private fun PgRowSet.toListDbRow(): DbRowSet {
        val columnNames = this.columnsNames()
        if (columnNames == null) {
            val columns = DbColumns(listOf("result"))
            return DbRowSet(columns, listOf(DbRow(columns, listOf(this.rowCount()))))
        } else {
            val columns = DbColumns(columnNames)
            val columnCount = columns.size
            val out = arrayListOf<DbRow>()
            for (row in this) out += DbRow(columns, (0 until columnCount).map { row.getValue(it) })
            return DbRowSet(columns, out)
        }
    }

    override fun close() {
        prepared.close()
    }
}
