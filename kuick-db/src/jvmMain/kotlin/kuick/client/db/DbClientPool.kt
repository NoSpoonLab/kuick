package kuick.client.db

import java.io.*

class DbClientPool(val maxConnections: Int = 30, val generator: suspend () -> DbConnection) : DbConnectionProvider, Closeable {
    companion object {
        operator fun invoke(driver: DbDriver, uri: String, maxConnections: Int = 30) = DbClientPool(maxConnections) {
            driver.connect(uri)
        }
    }

    private val connections = arrayListOf<DbConnection>()

    override suspend fun <T> get(callback: suspend (DbConnection) -> T): T {
        val connection = synchronized(connections) { if (connections.isNotEmpty()) connections.removeAt(connections.size - 1) else null }
                ?: generator()
        try {
            return callback(connection)
        } finally {
            synchronized(connections) { connections.add(connection) }
        }
    }

    override fun close() {
        for (con in synchronized(connections) { connections.toList().also { connections.clear() } }) {
            con.close()
        }
    }
}
