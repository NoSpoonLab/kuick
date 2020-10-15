package kuick.repositories.jasync

import kotlinx.coroutines.runBlocking
import kuick.repositories.desc
import kuick.repositories.gt
import kuick.repositories.gte
import kuick.repositories.like

fun main() = runBlocking {

    val pool = JasyncPool(
        "host",
        5432,
        "dbname",
        "user",
        "pwd"
    )

    data class User(val id: String, val name: String, val age: Int, val married: Boolean)

    val repo = ModelRepositoryJasync(User::class, "gkuser", User::id, pool)

    println("0. -----------------")
    repo.deleteBy(User::age gte 0)

    println("1. -----------------")
    repo.insertMany(listOf(
        User("mike", "Mike", 45, true),
        User("cris", "Cris", 43, true),
        User("jorge", "Jorge", 13, false),
        User("marcos", "Marcos", 10, false),
        User("daniel", "Daniel", 4, false)
    ))

    println("2. -----------------")
    val users = repo.findBy(User::age gt 1)
    println("Usuarios:")
    users.forEach {
        println(it)
    }

    println("3. -----------------")
    val mike = repo.findById("mike")!!
    repo.update(mike.copy(age = mike.age + 1))
    val mike2 = repo.findById("mike")!!
    assert(mike.age == mike2.age - 1)


    println("4. -----------------")
    val usersM = repo.findBy(User::name like "M%", orderBy = User::age.desc())
    assert(usersM.size == 2)
    assert(usersM[0].id == "mike")
    assert(usersM[1].id == "marcos")

}
