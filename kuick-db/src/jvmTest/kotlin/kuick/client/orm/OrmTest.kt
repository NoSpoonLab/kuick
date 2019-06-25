package kuick.client.orm

import kotlinx.coroutines.*
import kuick.client.db.*
import kuick.client.sql.*
import kuick.orm.*
import kotlin.test.*

class OrmTest {
    class LogDbPreparable(override val sql: SqlBuilder = SqlBuilder.Iso) : DbPreparable {
        val logs = arrayListOf<String>()

        private val responses = LinkedHashMap<String, DbRowSet>()

        fun register(vararg sqlToResponses: Pair<String, DbRowSet>) = this.apply {
            for ((sql, response) in sqlToResponses) {
                responses[sql] = response
            }
        }

        override suspend fun <T> prepare(sql: String, callback: suspend (DbPreparedStatement) -> T): T {
            return callback(object : DbPreparedStatement {
                override val sql: String = sql

                override suspend fun exec(vararg args: Any?): DbRowSet {
                    logs += sql
                    return responses[sql] ?: DbRowSet(DbColumns("result"), listOf())
                }
                override fun close() = Unit
            })
        }
    }

    data class Demo(val name: String)
    data class DemoNullable(val name: String?)

    @Test
    fun testSynchronizeTableNew() {
        runBlocking {

            LogDbPreparable().register(
            ).also { preparable ->
                preparable.synchronizeTable(TableDefinition(Demo::class))
                assertEquals(
                        listOf(
                                "CREATE TABLE IF NOT EXISTS \"Demo\"();",
                                "SHOW COLUMNS FROM \"Demo\";",
                                "ALTER TABLE \"Demo\" ADD COLUMN \"name\" VARCHAR NOT NULL;"
                        ),
                        preparable.logs
                )
            }
        }
    }

    @Test
    fun testSynchronizeTableAlreadyCreated() {
        runBlocking {
            LogDbPreparable().register(
                    "SHOW COLUMNS FROM \"Demo\";" to DbRowSet(mapOf("columnname" to "name"))
            ).also { preparable ->
                preparable.synchronizeTable(TableDefinition(Demo::class))
                assertEquals(
                        listOf(
                                "CREATE TABLE IF NOT EXISTS \"Demo\"();",
                                "SHOW COLUMNS FROM \"Demo\";"
                        ),
                        preparable.logs
                )
            }
        }
    }

    val definitions = TableDefinitions()

    @Test
    fun testTypedInsert() {
        runBlocking {
            LogDbPreparable().register(
            ).also { preparable ->
                val def = preparable.withDefinitions(definitions)
                def.insert(Demo("hello"))
                //preparable.insert(TableDefinition(Demo::class), Demo("hello"))
                //preparable.insert(TableDefinition(DemoNullable::class), DemoNullable(null))
                assertEquals(
                        listOf(
                                "INSERT INTO \"Demo\" (\"name\") VALUES (?);"
                        ),
                        preparable.logs
                )
            }
        }
    }
}
