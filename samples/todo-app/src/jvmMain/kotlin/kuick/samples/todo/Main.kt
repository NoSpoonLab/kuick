package kuick.samples.todo

import com.google.inject.*
import com.soywiz.korio.file.std.*
import com.soywiz.korte.*
import com.soywiz.korte.ktor.*
import com.soywiz.korte.ktor.Korte
import io.ktor.application.*
import io.ktor.request.*
import io.ktor.response.*
import io.ktor.routing.*
import io.ktor.server.engine.*
import io.ktor.server.netty.*
import kotlinx.coroutines.*
import kuick.client.db.*
import kuick.client.jdbc.*
import kuick.client.repositories.*
import kuick.core.*
import kuick.di.*
import kuick.ktor.*
import kuick.repositories.annotations.*
import java.util.*

suspend fun main(args: Array<String>) {
    embeddedServer(Netty, port = 8080) { module() }.start(wait = true)
}

@UseExperimental(KuickInternal::class)
fun Application.module() {
    val injector = Guice {
        bindPerCoroutineJob()
    }
    val perCoroutineJob = injector.get<PerCoroutineJob>()
    val db = DbClientPool { JdbcDriver.connectMemoryH2() }


    install(Korte) {
        (this as TemplateConfigWithTemplates).root(resourcesVfs["templates"])
    }
    perCoroutineJob.register { callback ->
        withContext(db) {
            callback()
        }
    }

    perCoroutineJob.runBlocking {
        Todo.Repository.init()
    }

    install(PerCoroutineJobFeature(perCoroutineJob))
    routing {
        get("/") {
            call.respondKorte("index.html", StandardModel(injector))
        }
        get("/remove/{id}") {
            val param = call.parameters["id"] ?: error("Id not specified")
            Todo.Repository.delete(Todo.Id(param))
            call.respondRedirect("/")
        }
        post("/") {
            val post = call.receiveParameters()
            Todo.Repository.insert(Todo(Todo.Id(), post["item"]!!))
            call.respondRedirect("/")
        }
    }
}

@Suppress("unused")
open class StandardModel(val injector: Injector) {
    suspend fun allTodos() = Todo.Repository.getAll()
}

abstract class AbstractId(override val id: String) : kuick.models.Id {
    override fun equals(other: Any?): Boolean = (other is AbstractId) && this.id == other.id
    override fun hashCode(): Int = id.hashCode()
    override fun toString(): String = id
}

data class Todo(
        val id: Id,
        @MaxLength(512) val text: String
) {
    fun removeLink() = "/remove/$id"

    class Id(id: String = UUID.randomUUID().toString()) : AbstractId(id)

    companion object {
        val Repository = DbModelRepository(Todo::id)
    }
}
