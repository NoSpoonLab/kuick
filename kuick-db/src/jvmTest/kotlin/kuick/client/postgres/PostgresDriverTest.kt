package kuick.client.postgres

import kotlinx.coroutines.*
import kuick.client.db.*
import org.arquillian.cube.containerobject.*
import org.arquillian.cube.docker.impl.client.containerobject.dsl.*
import org.jboss.arquillian.junit.*
import org.junit.runner.*
import kotlin.test.*

@RunWith(Arquillian::class)
class PostgresDriverTest {

    @DockerContainer
    var container = Container.withContainerName("kuick-postgres-test")
            .fromImage("postgres:11.2")
            .withPortBinding(5432)
            .withConnectionMode(ConnectionMode.START_AND_STOP_AROUND_CLASS)
            .withAwaitStrategy(AwaitBuilder.logAwait("database system is ready to accept connections"))
            .build()

    @Test
    @Ignore
    fun test() {
        runBlocking {
            PostgresDriver.connect("postgres://postgres@${container.ipAddress}:5432/postgres").use { connection ->
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