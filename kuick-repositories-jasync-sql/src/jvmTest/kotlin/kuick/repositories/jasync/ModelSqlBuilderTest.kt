package kuick.repositories.jasync

import kuick.repositories.AttributedModelQuery
import kuick.repositories.and
import kuick.repositories.desc
import kuick.repositories.eq
import kuick.repositories.gt
import kuick.repositories.gte
import kuick.repositories.lt
import kuick.repositories.lte
import kuick.repositories.not
import kuick.repositories.or
import kotlin.test.Test
import kotlin.test.assertEquals

class ModelSqlBuilderTest {

    data class User(val name: String, val age: Int, val married: Boolean)

    val mq = ModelSqlBuilder(User::class, "user")

    @Test
    fun `eq, gt, gte, lt, lte operators`() {

        assertEquals("name = 'Mike'", mq.toSql(User::name eq "Mike"))
        assertEquals("age = 46", mq.toSql(User::age eq 46))
        assertEquals("married = true", mq.toSql(User::married eq true))

        assertEquals("name > 'Mike'", mq.toSql(User::name gt "Mike"))
        assertEquals("age > 46", mq.toSql(User::age gt 46))

        assertEquals("name >= 'Mike'", mq.toSql(User::name gte "Mike"))
        assertEquals("age >= 46", mq.toSql(User::age gte 46))

        assertEquals("name < 'Mike'", mq.toSql(User::name lt "Mike"))
        assertEquals("age < 46", mq.toSql(User::age lt 46))

        assertEquals("name <= 'Mike'", mq.toSql(User::name lte "Mike"))
        assertEquals("age <= 46", mq.toSql(User::age lte 46))
    }

    @Test
    fun `composite expressions`() {

        assertEquals("(age >= 0) AND (age < 100)",
            mq.toSql((User::age gte  0) and (User::age lt 100)))

        assertEquals("((age >= 0) AND (age < 100)) OR ((age >= 200) AND (age < 300))",
            mq.toSql(
                ((User::age gte  0) and (User::age lt 100)) or ((User::age gte  200) and (User::age lt 300))
            )
        )

        assertEquals("NOT(age >= 0)",
            mq.toSql(not(User::age gte  0)))
    }

    @Test
    fun `insert, update, delete sql`() {
        assertEquals("INSERT INTO user (name, age, married) VALUES (?, ?, ?)", mq.insertSql)
        assertEquals("UPDATE user SET name = ?, age = ?, married = ? WHERE name = 'Mike'", mq.updateSql(User::name eq "Mike"))
        assertEquals("DELETE FROM user WHERE name = 'Mike'", mq.deleteSql(User::name eq "Mike"))
    }


    @Test
    fun `select sql`() {
        assertEquals("SELECT name, age, married FROM user WHERE name = 'Mike'", mq.selectSql(User::name eq "Mike"))
    }

    @Test
    fun `prepared select`() {
        assertEquals(
            ModelSqlBuilder.PreparedSql("SELECT name, age, married FROM user WHERE name = ?", listOf("Mike")),
            mq.selectPreparedSql(User::name eq "Mike"))
    }

    @Test
    fun `prepared select complex`() {
        assertEquals(
            ModelSqlBuilder.PreparedSql("SELECT name, age, married FROM user WHERE name = ? SKIP 1 LIMIT 2 ORDER BY name DESC", listOf("Mike")),
            mq.selectPreparedSql(AttributedModelQuery(base = User::name eq "Mike", skip = 1, limit = 2, orderBy = User::name.desc()) ))
    }

    @Test
    fun `prepared insert`() {
        assertEquals(
            ModelSqlBuilder.PreparedSql("INSERT INTO user (name, age, married) VALUES (?, ?, ?)",
                listOf("Mike", 46, true)),
            mq.insertPreparedSql(User("Mike", 46, true)))
    }

    @Test
    fun `prepared update`() {
        assertEquals(
            ModelSqlBuilder.PreparedSql("UPDATE user SET name = ?, age = ?, married = ? WHERE name = ?",
                listOf("Mike2", 46, true, "Mike")),
            mq.updatePreparedSql(User("Mike2", 46, true), User::name eq "Mike")
        )
    }

    @Test
    fun `prepared delete`() {
        assertEquals(
            ModelSqlBuilder.PreparedSql("DELETE FROM user WHERE name = ?", listOf("Mike")),
            mq.deletePreparedSql(User::name eq "Mike")
        )
    }

}
