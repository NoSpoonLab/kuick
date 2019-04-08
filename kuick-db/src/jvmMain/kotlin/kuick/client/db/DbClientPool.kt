package kuick.client.db

class DbClientPool(val maxConnections: Int = 30, val generator: suspend () -> DbConnection) {
    companion object {
        operator fun invoke(driver: DbDriver, uri: String, maxConnections: Int = 30) = DbClientPool(maxConnections) {
            driver.connect(uri)
        }
    }

    private val connections = arrayListOf<DbConnection>()

    suspend fun <T> get(callback: suspend (DbConnection) -> T): T {
        val connection = synchronized(connections) { if (connections.isNotEmpty()) connections.removeAt(connections.size - 1) else null }
                ?: generator()
        try {
            return callback(connection)
        } finally {
            synchronized(connections) { connections.add(connection) }
        }
    }

    suspend fun <T> getTransaction(callback: suspend (DbTransaction) -> T): T {
        return get { connection ->
            connection.transaction {
                callback(it)
            }
        }
    }
}
