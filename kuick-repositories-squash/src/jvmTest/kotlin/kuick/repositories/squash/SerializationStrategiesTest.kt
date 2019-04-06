package kuick.repositories.squash

import org.jetbrains.squash.definition.*
import java.util.*
import kotlin.test.*

class SerializationStrategiesTest {
    val dateProp = Date()

    @Test
    fun test() {
        val strategies = SerializationStrategies()
                .withSerialization(dateSerializationAsLong)

        val table = TableDefinition("test")

        strategies.tryGetColumnDefinition(table, PropertyInfo(this::dateProp)).also { def ->
            assertNotNull(def)
            assertEquals(LongColumnType, def.type)
        }
    }
}