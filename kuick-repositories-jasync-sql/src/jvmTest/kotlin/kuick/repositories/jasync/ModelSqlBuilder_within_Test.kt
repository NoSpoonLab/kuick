package kuick.repositories.jasync

import kuick.models.Id
import kuick.repositories.*
import kotlin.test.Test
import kotlin.test.assertEquals

class ModelSqlBuilder_within_Test {

    data class User(val name: String, val age: Int, val married: Boolean)
    val mq = ModelSqlBuilder(User::class, "user")

    data class UserId(override val id: String): Id
    data class User2(val id: UserId, val name: String)
    val mq2 = ModelSqlBuilder(User2::class, "user")


    @Test
    fun `within operator`() {
        assertEquals("name in ('Mike', 'Marcos')", mq.toSql(User::name within setOf("Mike", "Marcos") ))
    }


    @Test
    fun `prepared within operator`() {
        assertEquals(
            ModelSqlBuilder.PreparedSql("SELECT id, name FROM user WHERE id in (?, ?)", listOf("Mike", "Marcos")),
            mq2.selectPreparedSql(User2::id within setOf(UserId("Mike"), UserId("Marcos")) )
        )
    }


}
