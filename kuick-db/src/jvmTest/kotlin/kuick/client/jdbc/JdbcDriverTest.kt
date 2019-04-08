package kuick.client.jdbc

import kotlinx.coroutines.*
import kotlin.test.*

class JdbcDriverTest {
    @Test
    fun test() {
        runBlocking {
            JdbcDriver.connect("jdbc:h2:mem:0").use { connection ->
                connection.transaction { transaction ->
                    assertEquals(1, transaction.query("SELECT 1;").first().get(0))
                }
            }
        }
    }
}