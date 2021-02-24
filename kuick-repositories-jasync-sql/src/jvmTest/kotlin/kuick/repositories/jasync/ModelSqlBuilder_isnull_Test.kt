package kuick.repositories.jasync

import kuick.repositories.*
import kotlin.test.Test
import kotlin.test.assertEquals

class ModelSqlBuilder_isnull_Test {

    data class User(val name: String, val surname: String?)
    val mq = ModelSqlBuilder(User::class, "user")


    @Test
    fun `isNull operator`() {
        assertEquals("surname IS NULL", mq.toSql( User::surname.isNull() ))
    }

    @Test
    fun `select isNull operator`() {
        assertEquals("SELECT name, surname FROM user WHERE (surname IS NULL) AND (name = 'Mike')", mq.selectSql((User::surname.isNull()) and (User::name eq "Mike")))
        assertEquals(
            ModelSqlBuilder.PreparedSql("SELECT name, surname FROM user WHERE (surname IS NULL) AND (name = ?)", listOf("Mike")),
            mq.selectPreparedSql((User::surname.isNull()) and (User::name eq "Mike"))
        )
    }


    @Test
    fun `not isNull operator`() {
        assertEquals("NOT(surname IS NULL)", mq.toSql( not(User::surname.isNull()) ))
    }

    @Test
    fun `select not isNull operator`() {
        assertEquals("SELECT name, surname FROM user WHERE NOT(surname IS NULL)", mq.selectSql(not(User::surname.isNull())))
        assertEquals(
            ModelSqlBuilder.PreparedSql("SELECT name, surname FROM user WHERE NOT(surname IS NULL)", listOf()),
            mq.selectPreparedSql(not(User::surname.isNull()))
        )
    }


}
