package kuick.samples.todo2

import io.ktor.application.Application
import io.ktor.routing.routing
import io.ktor.server.engine.embeddedServer
import io.ktor.server.netty.Netty
import kuick.api.rest.get
import kuick.api.rest.post
import kuick.api.rest.restRoute
import kuick.api.rpc.rpcRoute
import kuick.client.db.DbClientPool
import kuick.client.jdbc.JdbcDriver
import kuick.di.Guice
import kuick.di.bindPerCoroutineJob
import kuick.ktor.installContextPerRequest
import kuick.ktor.installHttpExceptionsSupport

suspend fun main(args: Array<String>) {
    embeddedServer(Netty, port = 8080) { module() }.start(wait = true)
}

fun Application.module() {
    val injector = Guice {
        bindPerCoroutineJob()
        configure()
    }

    installContextPerRequest(injector, DbClientPool { JdbcDriver.connectMemoryH2() }) {
        injector.getInstance(TodoRepository::class.java).init()
        injector.getInstance(UserRepository::class.java).init()
    }
    installHttpExceptionsSupport()

    routing {
        rpcRoute<TodoApi>(injector)
        rpcRoute<UserApi>(injector)

        restRoute<TodoApi>(injector, "todos") {
            get<Todo>(TodoApi::getAll) {
                withFieldsParameter()
                withIncludeParameter(
                        Todo::owner to { id -> injector.getInstance(UserApi::class.java).getOne(id) }
                )
            }
            post(TodoApi::add)

//            route("/{id}") {
//                get(TodoApi::getOne)
//            }
        }
        restRoute<UserApi>(injector, "users") {
            post(UserApi::add)
        }

//        graphQlRouting<TodoApi>(injector)
    }
}

