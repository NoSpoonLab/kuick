package kuick.repositories.jasync

import kuick.repositories.isNull
import kuick.repositories.not
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
    fun `not isNull operator`() {
        assertEquals("NOT(surname IS NULL)", mq.toSql( not(User::surname.isNull()) ))
    }


}
