package kuick.repositories.squash

import kuick.models.*
import org.jetbrains.squash.definition.*
import org.jetbrains.squash.results.*
import java.lang.reflect.*
import java.util.*
import kotlin.reflect.*
import kotlin.reflect.full.*
import kotlin.reflect.jvm.*
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

        assertEquals("""{"a":10,"b":"hello"}""", JsonSerializationStrategy.tryEncodeValue(MyDataClass(10, "hello")))
        //assertEquals(MyDataClass(10, "hello"), JsonSerializationStrategy.tryDecodeValueLazy(MyDataClass::class.starProjectedType) { """{"a":10,"b":"hello"}""" })
        assertEquals(MyDataClass(10, "hello"), JsonSerializationStrategy.tryDecodeValueLazy(::myDataProp.returnType) { """{"a":10,"b":"hello"}""" })
    }

    val testFieldList = listOf(MyDataClass(1, "one"), MyDataClass(2, "two"))

    @Test
    fun testJsonSerializationList() {
        assertEquals("""[{"a":1,"b":"one"},{"a":2,"b":"two"}]""", JsonSerializationStrategy.tryEncodeValue(testFieldList))
        assertEquals(listOf(MyDataClass(1, "one"), MyDataClass(2, "two")), JsonSerializationStrategy.tryDecodeValueLazy(::testFieldList.returnType) { """[{"a":1,"b":"one"},{"a":2,"b":"two"}]""" })
    }
}
