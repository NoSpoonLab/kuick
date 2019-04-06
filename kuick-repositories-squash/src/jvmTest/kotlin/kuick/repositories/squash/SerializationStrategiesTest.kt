package kuick.repositories.squash

import kuick.models.*
import org.jetbrains.squash.definition.*
import java.util.*
import kotlin.test.*

class SerializationStrategiesTest {
    class OtherId(override val id: String) : Id

    val dateProp = Date()
    val otherIdProp = OtherId("test")
    val table = TableDefinition("test")

    @Test
    fun test() {
        val strategies = SerializationStrategies()
                .with(dateSerializationAsLong)

        strategies.tryGetColumnDefinition(table, PropertyInfo(this::dateProp)).also { def ->
            assertNotNull(def)
            assertEquals(LongColumnType, def.type)
        }
    }

    @Test
    fun testId() {
        defaultSerializationStrategies.tryGetColumnDefinition(table, PropertyInfo(this::otherIdProp)).also { def ->
            assertNotNull(def)
            assertTrue(def.type is StringColumnType)
        }
    }

    @Test
    fun testTypedSerializationStrategy() {
        assertEquals(SerializationStrategy.Unhandled, longSerialization.tryDecodeValue(10))
        assertEquals(10L, longSerialization.tryDecodeValue(10L))
    }
}