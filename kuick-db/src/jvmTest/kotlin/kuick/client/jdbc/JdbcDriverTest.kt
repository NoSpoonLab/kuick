package kuick.client.jdbc

import kotlinx.coroutines.*
import kuick.client.db.*
import kuick.client.db.query
import kuick.client.orm.*
import kuick.orm.*
import java.util.*
import kotlin.test.*

class JdbcDriverTest {
    @Test
    fun test() {
        runBlocking {
            JdbcDriver.connectMemoryH2().use { connection ->
                val sql = connection.sql
                connection.transaction { transaction ->
                    transaction.createTable("test")

                    println(transaction.listTables())

                    transaction.addColumn("test", "demo", sql.typeVarchar(64), true)

                    println(transaction.listColumns("test"))

                    transaction.insert("test", listOf("demo"), listOf("hello"), listOf("world"))
                    transaction.insert("test", listOf("demo"), listOf("test"))
                    transaction.createIndex("test", listOf("demo"), unique = true)
                    //transaction.insert("test", listOf("demo"), listOf("test"))
                    for (item in transaction.query("SELECT * FROM ${sql.quoteTableName("test")};")) {
                        println(item)
                    }
                    transaction.deleteAll("test")
                    for (item in transaction.query("SELECT * FROM ${sql.quoteTableName("test")};")) {
                        println(item)
                    }
                    transaction.dropTable("test")
                    assertEquals(1, transaction.query("SELECT 1;")[0, 0])
                }
            }
        }
    }
}
