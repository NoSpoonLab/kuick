package kuick.client.log

import kotlinx.coroutines.*
import kuick.client.db.*
import kuick.client.jdbc.*
import kotlin.test.*

class LogDbDriverTest {
    @Test
    fun test() {
        runBlocking {
            val log = DbDriverLog.ToArray()
            JdbcDriver.connectJdbcMemoryH2().log(log).use { connection ->
                connection.query("SELECT 1;")
            }
            assertEquals(
                    """
                    |connection.log.start
                    ||transaction.start
                    |||prepare.start SELECT 1;
                    ||||exec.start SELECT 1;: []
                    ||||exec.result SELECT 1; -> 1
                    |||prepared.close
                    |||prepare.end SELECT 1;
                    ||transaction.end
                    |connection.close
                    """.trimIndent(),
                    log.log.joinToString("\n")
            )
        }
    }
}