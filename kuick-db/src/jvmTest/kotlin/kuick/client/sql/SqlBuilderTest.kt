package kuick.client.sql

import kuick.orm.*
import kuick.repositories.*
import kotlin.test.*

class SqlBuilderTest {
    @Test
    fun test() {
        data class Demo(val id: String)
        val table = TableDefinition<Demo>()
        assertEquals("SELECT * FROM \"Demo\" WHERE \"id\" = ?;", SqlBuilder.Iso.sqlSelect(Demo::id eq "test", table).sql)
        assertEquals("SELECT * FROM \"Demo\" WHERE \"id\" = ? ORDER BY \"id\" DESC LIMIT 10 OFFSET 0;", SqlBuilder.Iso.sqlSelect(AttributedModelQuery(Demo::id eq "test", limit = 10, orderBy = Demo::id.desc()), table).sql)
        assertEquals("SELECT * FROM \"Demo\" WHERE (\"id\" = ? AND \"id\" <> ?);", SqlBuilder.Iso.sqlSelect((Demo::id eq "test") and (Demo::id ne "demo"), table).sql)
    }
}