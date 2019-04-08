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
    override val sql = SqlBuilder.Iso

    override suspend fun <T> transaction(callback: suspend (DbTransaction) -> T): T {
        return connection.transaction { callback(PostgressTransaction(this, it)) }
    }

    override fun close() {
        closeVertx?.close()
        connection.close()
    }
}

class PostgressTransaction(val connection: PostgressConnection, val transaction: PgTransaction) : DbTransaction {
    override val sql: SqlBuilder get() = connection.sql
    override suspend fun prepare(sql: String): DbPreparedStatement =
            PostgresPreparedStatement(transaction.prepareAwait(sql))
}

class PostgresPreparedStatement(val prepared: PgPreparedQuery) : DbPreparedStatement {
    override suspend fun exec(vararg args: Any?): DbRowSet {
        val tuple = when (args.size) {
            0 -> Tuple.tuple()
            1 -> Tuple.of(args.first())
            else -> Tuple.of(args.first(), *args.drop(1).toTypedArray())
        }
        return prepared.executeAwait(tuple).toListDbRow()
    }

    private fun PgRowSet.toListDbRow(): DbRowSet {
        val columns = DbColumns(this.columnsNames())
        val columnCount = columns.size
        val out = arrayListOf<DbRow>()
        for (row in this) out += DbRow(columns, (0 until columnCount).map { row.getValue(it) })
        return DbRowSet(columns, out)
    }

    override fun close() {
        prepared.close()
    }
}
