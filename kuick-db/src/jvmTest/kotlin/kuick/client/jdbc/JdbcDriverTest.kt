package kuick.client.jdbc

import kotlinx.coroutines.*
import kuick.client.db.*
import kotlin.test.*

class JdbcDriverTest {
    @Test
    fun test() {
        runBlocking {
            JdbcDriver.connect("jdbc:h2:mem:0").use { connection ->
                connection.transaction { transaction ->
                    transaction.createTable("test")
                    transaction.addColumn("test", "demo", "VARCHAR(64)")
                    transaction.insert("test", listOf("demo"), listOf("hello"), listOf("world"))
                    transaction.insert("test", listOf("demo"), listOf("test"))
                    transaction.createIndex("test", listOf("demo"), unique = true)
                    //transaction.insert("test", listOf("demo"), listOf("test"))
                    for (item in transaction.query("SELECT * FROM ${transaction.sql.quoteTableName("test")};")) {
                        println(item)
                    }
                    transaction.dropTable("test")
                    assertEquals(1, transaction.query("SELECT 1;").first().get(0))
                }
            }
        }
    }
}