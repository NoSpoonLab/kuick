package kuick.client.postgres

import kotlinx.coroutines.*
import kuick.client.db.*
import kotlin.test.*

class PostgresDriverTest {
    @Test
    @Ignore
    fun test() {
        runBlocking {
            PostgresDriver.connect("postgres://postgres@localhost:5432/postgres").use { connection ->
                val sql = connection.sql
                connection.transaction { transaction ->
                    kotlin.runCatching {
                        println(transaction.dropTable("test"))
                    }
                }
                connection.transaction { transaction ->
                    transaction.createTable("test")
                    println(transaction.listTables())
                    transaction.addColumn("test", "demo", sql.typeVarchar(64))

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