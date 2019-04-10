package kuick.samples.serverless

import kotlinx.coroutines.*
import kuick.client.repositories.*
import kuick.headless.*

fun main(args: Array<String>) {
    runBlocking {
    }
}

@Route("/init")
class InitHandler : KuickHeadlessHandler<String>() {
    override suspend fun handler(): String {
        User.Repo.init()
        return "ok"
    }
}

@Route("/hello")
@HandlesEvent("myevent")
class HelloHandler : KuickHeadlessHandler<String>() {
    override suspend fun handler(): String {
        User.Repo.insert(User("test"))
        return "HELLO WORLD! : ${User.Repo.getAll()}"
    }
}

class User(
    val name: String
) {
    companion object {
        val Repo = DbModelRepository(User::name)
    }
}
