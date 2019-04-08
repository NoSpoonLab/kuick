package kuick.client.postgres

import io.reactiverse.kotlin.pgclient.*
import io.reactiverse.kotlin.pgclient.PgClient
import io.reactiverse.pgclient.*
import io.vertx.core.*
import kuick.client.db.*

object PostgresDriver : DbDriver {
    override suspend fun connect(url: String): DbConnection {
        val vertx = Vertx.vertx()
        return PostgressConnection(vertx, PgClient.connect(vertx, url))
    }
}

class PostgressConnection(val closeVertx: Vertx?, val connection: PgConnection) : DbConnection {
    override suspend fun <T> transaction(callback: suspend (DbTransaction) -> T): T {
        return connection.transaction { callback(PostgressTransaction(it)) }
    }

    override fun close() {
        closeVertx?.close()
        connection.close()
    }
}

class PostgressTransaction(val transaction: PgTransaction) : DbTransaction {
    override suspend fun prepare(sql: String): DbPreparedStatement =
            PostgresPreparedStatement(transaction.prepareAwait(sql))
}

class PostgresPreparedStatement(val prepared: PgPreparedQuery) : DbPreparedStatement {
    override suspend fun exec(vararg args: Any?): List<DbRow> {
        val tuple = when (args.size) {
            0 -> Tuple.tuple()
            1 -> Tuple.of(args.first())
            else -> Tuple.of(args.first(), *args.drop(1).toTypedArray())
        }
        return prepared.executeAwait(tuple).toListDbRow()
    }

    private fun PgRowSet.toListDbRow(): List<DbRow> {
        val columns = DbColumns(this.columnsNames())
        val columnCount = columns.size
        val out = arrayListOf<DbRow>()
        for (row in this) out += DbRow(columns, (0 until columnCount).map { row.getValue(it) })
        return out
    }

    override fun close() {
        prepared.close()
    }
}
