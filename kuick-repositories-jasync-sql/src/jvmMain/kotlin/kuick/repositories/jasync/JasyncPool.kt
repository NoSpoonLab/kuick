package kuick.repositories.jasync

import com.github.jasync.sql.db.QueryResult
import com.github.jasync.sql.db.SuspendingConnection
import com.github.jasync.sql.db.asSuspending
import com.github.jasync.sql.db.pool.ConnectionPool
import com.github.jasync.sql.db.postgresql.PostgreSQLConnection
import com.github.jasync.sql.db.postgresql.PostgreSQLConnectionBuilder
import kuick.env.Environment
import java.util.*

class JasyncPool(
    host: String,
    port: Int,
    database: String,
    username: String,
    password: String,
    applicationName: String = "kuick-jaync-pool",
    maxActiveConnections: Int = 2,
    val debug: Boolean = false
) {
    private val pool: ConnectionPool<PostgreSQLConnection>

    init {
        pool = PostgreSQLConnectionBuilder.createConnectionPool {
            this.host = host
            this.port = port
            this.database = database
            this.username = username
            this.password = password
            this.applicationName = applicationName
            this.maxActiveConnections = maxActiveConnections
        }
        log("JasyncPool ---------------------")
        log("Created conection pool:")
        log("  host                 : $host")
        log("  port                 : $port")
        log("  database             : $database")
        log("  username             : $username")
        log("  applicationName      : $applicationName")
        log("  maxActiveConnections : $maxActiveConnections")
        log("/JasyncPool ---------------------")
    }

    fun connection(): SuspendingConnection = pool.asSuspending

    suspend fun query(sql: String): QueryResult = execute(sql, null)

    suspend fun prepQuery(sql: String, values: List<Any?>): QueryResult = execute(sql, values)

    private suspend fun execute(sql: String, values: List<Any?>?): QueryResult {
        try {
            val begin = System.currentTimeMillis()
            val qr = values
                ?.let { connection().sendPreparedStatement(sql, values) }
                ?: connection().sendQuery(sql)
            val end = System.currentTimeMillis()
            val lapse = (end - begin)
            debug("[SQL] $sql ${values ?: ""} | ${qr.rowsAffected} rows, $lapse ms")
            return qr
        } catch (t: Throwable) {
            System.err.println("SQL ERROR ---------------------")
            System.err.println("SQL:    $sql")
            System.err.println("Values: $values")
            throw t
        }
    }

    private fun log(msg: String) {
        println("${Date()} $msg")
    }

    private fun debug(msg: String) {
        if (debug) log(msg)
    }


    companion object {

        fun fromEnvironment(prefix: String = ""): JasyncPool {

            fun env(key: String, default: String? = null): String =
                Environment.env("$prefix$key", default)

            return JasyncPool(
                host = env("DB_HOST"),
                port = env("DB_PORT").toInt(),
                database = env("DB_DATABASE"),
                username = env("DB_USERNAME"),
                password = env("DB_PASSWORD"),
                maxActiveConnections = env("DB_MAX_ACTIVE_CONNECTIONS", "2").toInt(),
                debug = env("DB_DEBUG", "false").toBoolean()
            )
        }

    }
}
