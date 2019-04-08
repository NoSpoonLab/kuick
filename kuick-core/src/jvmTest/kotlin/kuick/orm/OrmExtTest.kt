package kuick.orm

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
    fun testDbName() {
        @DbName("test") data class MyTable2(@DbName("myid") val id: String)
        assertEquals("test", TableDefinition(MyTable2::class).name)
        assertEquals("myid", TableDefinition(MyTable2::class).columns.first().name)
    }
}