package kuick.client.log

import kuick.client.db.*
import kuick.client.sql.*
import java.io.*

interface DbDriverLog {
    fun log(message: String)
    class ToArray(val log: ArrayList<String> = arrayListOf()) : DbDriverLog {
        override fun log(message: String): Unit = run { log.add(message) }
    }
    class ToStream(val stream: PrintStream = System.out) : DbDriverLog {
        override fun log(message: String) = stream.println(message)
    }
}
fun DefaultDbDriverLog() = DbDriverLog.ToStream()

fun DbDriver.log(log: DbDriverLog = DefaultDbDriverLog()) = LogDbDriver(this, log)
class LogDbDriver(val parent: DbDriver, val log: DbDriverLog) : DbDriver {
    override suspend fun connect(url: String): DbConnection = parent.connect(url).log(log)
}

fun DbConnection.log(log: DbDriverLog = DefaultDbDriverLog()) = LogDbConnection(this, log)
class LogDbConnection(val parent: DbConnection, val log: DbDriverLog) : DbConnection  {
    override val sql: SqlBuilder get() = parent.sql

    override suspend fun <T> transaction(callback: suspend (DbTransaction) -> T): T {
        return parent.transaction {
            log.log("transaction.start")
            try {
                val loggedTransaction = it.log(log)
                callback(loggedTransaction)
            } finally {
                log.log("transaction.end")
            }
        }
    }

    override fun close() {
        log.log("connection.close")
        parent.close()
    }
}

fun DbTransaction.log(log: DbDriverLog = DefaultDbDriverLog()) = LogDbTransaction(this, log)
class LogDbTransaction(val parent: DbTransaction, val log: DbDriverLog) : DbTransaction by parent {
    override suspend fun prepare(sql: String): DbPreparedStatement {
        log.log("prepare $sql")
        return parent.prepare(sql).log(log)
    }
}

fun DbPreparedStatement.log(log: DbDriverLog = DefaultDbDriverLog()) = LogDbPreparedStatement(this, log)
class LogDbPreparedStatement(val parent: DbPreparedStatement, val log: DbDriverLog) : DbPreparedStatement by parent {
    override suspend fun exec(vararg args: Any?): DbRowSet {
        log.log("exec $sql: ${args.toList()}")
        val result = parent.exec(*args)
        log.log(" result -> ${result.size}")
        return result
    }

    override fun close() {
        log.log("prepared.close")
        return parent.close()
    }
}
