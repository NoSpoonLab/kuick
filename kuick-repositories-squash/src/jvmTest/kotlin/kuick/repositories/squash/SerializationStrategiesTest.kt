package kuick.repositories.squash

import kuick.models.*
import org.jetbrains.squash.definition.*
import java.util.*
import kotlin.test.*

class SerializationStrategiesTest {
    class OtherId(override val id: String) : Id

    val dateProp = Date()
    val otherIdProp = OtherId("test")

    fun table() = TableDefinition("test")

    @Test
    fun test() {
        dateSerializationAsLong.tryGetColumnDefinition(table(), PropertyInfo(this::dateProp)).also { def ->
            assertNotNull(def)
            assertEquals(LongColumnType, def.type)
        }
        dateSerializationAsDateTime.tryGetColumnDefinition(table(), PropertyInfo(this::dateProp)).also { def ->
            assertNotNull(def)
            assertEquals(DateTimeColumnType, def.type)
        }
    }

    @Test
    fun testId() {
        defaultSerializationStrategies.tryGetColumnDefinition(table(), PropertyInfo(this::otherIdProp)).also { def ->
            assertNotNull(def)
            assertTrue(def.type is StringColumnType)
        }
    }

    @Test
    fun testTypedSerializationStrategy() {
        assertEquals(SerializationStrategy.Unhandled, longSerialization.tryDecodeValue(10))
        assertEquals(10L, longSerialization.tryDecodeValue(10L))
    }

    @Test
    fun testWithCombine() {
        assertTrue((longSerialization + dateSerializationAsLong) is TypedSerializationStrategies)
        assertTrue((longSerialization + dateSerializationAsLong + stringSerialization) is TypedSerializationStrategies)
        assertTrue((longSerialization + IdSerializationStrategy) is SerializationStrategies)
        (longSerialization + IdSerializationStrategy + dateSerializationAsLong + stringSerialization).also { strats ->
            assertTrue(strats is SerializationStrategies)
            assertEquals(3, strats.strategies.size)
            assertTrue(strats.strategies[0] is TypedSerializationStrategy<*>)
            assertTrue(strats.strategies[1] is IdSerializationStrategy)
            assertTrue(strats.strategies[2] is TypedSerializationStrategies)
        }
    }
}