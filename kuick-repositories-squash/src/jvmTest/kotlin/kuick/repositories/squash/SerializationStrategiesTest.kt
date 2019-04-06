package kuick.repositories.squash

import kuick.models.*
import org.jetbrains.squash.definition.*
import org.jetbrains.squash.results.*
import java.util.*
import kotlin.reflect.*
import kotlin.test.*

class SerializationStrategiesTest {
    class OtherId(override val id: String) : Id

    val dateProp = Date()
    val otherIdProp = OtherId("test")

    fun table() = TableDefinition("test")

    data class MyDataClass(val a: Int, val b: String)

    @JvmField
    var myDataProp = MyDataClass(10, "hello")

    @Test
    fun testDate() {
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
        assertEquals(SerializationStrategy.Unhandled, longSerialization.tryEncodeValue(10))
        assertEquals(10L, longSerialization.tryEncodeValue(10L))
    }

    @Test
    fun testWithGraph() {
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

    @Test
    fun testPrioritySerialization() {
        val defaultSet = defaultSerializationStrategies
        val customSet = dateSerializationAsDateTime + defaultSerializationStrategies

        defaultSet.tryGetColumnDefinition(table(), PropertyInfo(this::dateProp)).also { def ->
            assertNotNull(def)
            assertEquals(LongColumnType, def.type)
        }
        customSet.tryGetColumnDefinition(table(), PropertyInfo(this::dateProp)).also { def ->
            assertNotNull(def)
            assertEquals(DateTimeColumnType, def.type)
        }
    }

    @Test
    fun testJsonSerialization() {
        JsonSerializationStrategy.tryGetColumnDefinition(table(), PropertyInfo(this::dateProp)).also { def ->
            assertNotNull(def)
            assertTrue(def.type is StringColumnType)
        }

        // @TODO: This whole test shows that this interface is shitty as hell

        assertEquals("""{"a":10,"b":"hello"}""", JsonSerializationStrategy.tryEncodeValue(MyDataClass(10, "hello")))

        val resultRow = object : ResultRow {
            override fun columnValue(type: KClass<*>, index: Int): Any? = """{"a":10,"b":"hello"}"""
            override fun columnValue(type: KClass<*>, columnName: String, tableName: String?): Any? = """{"a":10,"b":"hello"}"""
        }

        //JsonSerializationStrategy.tryReadColumnValue(SerializationStrategiesTest::class.java.getField(::myDataProp.name), resultRow, "demo", "test", MyDataClass::class)
    }
}