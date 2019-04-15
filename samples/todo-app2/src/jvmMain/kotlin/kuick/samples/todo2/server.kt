package kuick.samples.todo2

import io.ktor.application.Application
import io.ktor.server.engine.embeddedServer
import io.ktor.server.netty.Netty
import kuick.client.db.DbClientPool
import kuick.client.jdbc.JdbcDriver
import kuick.di.Guice
import kuick.di.bindPerCoroutineJob
import kuick.ktor.installContextPerRequest
import kuick.ktor.installHttpExceptionsSupport
import kuick.samples.todo2.infrastructure.*

suspend fun main(args: Array<String>) {
    embeddedServer(Netty, port = 8080) { module() }.start(wait = true)
}

fun Application.module() {
    val injector = Guice {
        bindPerCoroutineJob()
        configure()
    }

    // What's this? Database config also in Guice module
    installContextPerRequest(injector, DbClientPool { JdbcDriver.connectMemoryH2() }) {
        injector.getInstance(TodoRepository::class.java).init()
        injector.getInstance(UserRepository::class.java).init()
    }
    installHttpExceptionsSupport()

    kuickRouting {
        rpcRouting<TodoApi>(injector)
        rpcRouting<UserApi>(injector)


        restRouting<TodoApi>(injector, "todos") {
            get(TodoApi::getAll) {
                withFieldsParameter()
                withIncludeParameter(
                        Todo::owner to UserApi::getOne
                )
            }
            post(TodoApi::add)
        }
        restRouting<UserApi>(injector, "users") {
            post(UserApi::add)
        }

//        graphQlRouting<TodoApi>(injector)
    }
}

