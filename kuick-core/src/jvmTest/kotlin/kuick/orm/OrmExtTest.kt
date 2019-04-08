package kuick.orm

import kuick.models.*
import kuick.repositories.annotations.*
import kotlin.test.*

class OrmExtTest {
    @Test
    fun testBasic() {
        data class MyTable(val id: String)

        val table = TableDefinition(MyTable::class)
        assertEquals(MyTable::class.simpleName, table.name)
        assertEquals(1, table.columns.size)
        assertEquals(MyTable::id.name, table.columns[0].name)
    }

    @Test
    fun testBasicTypes() {
        data class MyTable(val id: String, val item: Int)
        val table = TableDefinition(MyTable::class)
        assertEquals(ColumnType.VARCHAR(null), table.columns.first().columnType)
        assertEquals(ColumnType.INT, table.columns.last().columnType)
    }

    @Test
    fun testDbName() {
        @DbName("test") data class MyTable2(@DbName("myid") val id: String)
        assertEquals("test", TableDefinition(MyTable2::class).name)
        assertEquals("myid", TableDefinition(MyTable2::class).columns.first().name)
        assertEquals(ColumnType.VARCHAR(null), TableDefinition(MyTable2::class).columns.first().columnType)
    }

    data class MyId(override val id: String) : Id

    @Test
    fun testTyping() {
        data class MyTable(val id: String)
        data class MyTable2(val id: MyId)
        // Untype
        assertEquals(mapOf("id" to "hello"), TableDefinition(MyTable::class).untype(MyTable("hello")))
        assertEquals(mapOf("id" to "hello"), TableDefinition(MyTable2::class).untype(MyTable2(MyId("hello"))))
        // Type
        assertEquals(MyTable("hello"), TableDefinition<MyTable>().type(mapOf("id" to "hello")))
        assertEquals(MyTable2(MyId("hello")), TableDefinition<MyTable2>().type(mapOf("id" to "hello")))
    }
}
