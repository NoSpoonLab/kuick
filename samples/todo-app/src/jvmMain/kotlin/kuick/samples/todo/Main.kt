package kuick.samples.todo

import com.google.inject.*
import com.soywiz.korio.file.std.*
import com.soywiz.korte.*
import com.soywiz.korte.ktor.Korte
import io.ktor.application.*
import io.ktor.http.*
import io.ktor.server.engine.*
import io.ktor.server.netty.*
import kuick.client.db.*
import kuick.client.jdbc.*
import kuick.client.repositories.*
import kuick.di.*
import kuick.ktor.*
import kuick.repositories.annotations.*
import kuick.repositories.patterns.*
import kuick.utils.*

suspend fun main(args: Array<String>) {
    embeddedServer(Netty, port = 8080) { module() }.start(wait = true)
}

fun Application.installKorte(templates: Templates) = run { install(Korte) { this.templates = templates } }

fun Application.module() {
    val injector = Guice {
        bindPerCoroutineJob()
    }

    val templates = Templates(resourcesVfs["templates"])

    installKorte(templates)
    installContextPerRequest(injector, DbClientPool { JdbcDriver.connectMemoryH2() }) { Todo.CachedRepository.init() }
    installHttpExceptionsSupport()

    kuickRouting {
        get("/sample-html") { "Hello world!" }
        get("/sample-txt") { "Hello world!".withContentType(ContentType.Text.Plain) }
        get("/") {
            templates.render("index.html", StandardModel(injector))
        }
        get("/remove/{id}") {
            val param = param("id")
            Todo.CachedRepository.delete(Todo.Id(param))
            redirect("/")
        }
        post("/") {
            val item = post("item")
            Todo.CachedRepository.insert(Todo(Todo.Id(), item))
            redirect("/")
        }
    }
}

//class KorteResult(val templates: Templates, val template: String, val model: Any?) : SuspendingResult<String> {
//    override suspend fun get(): String = templates.render(template, model)
//}
//
//fun Templates.result(template: String, model: Any?) = KorteResult(this, template, model)

/*
@Suppress("unused")
class ApplicationRoutes(val injector: Injector) {
    val templates = injector.get<Templates>()

    class RootLocation() : Location("/")
    class RemoveLocation(@MaxLength(64) val id: String) : Location("/remove/{id}")

    @Get
    @RouteLocation(RemoveLocation::class)
    suspend fun getIndex(): String {
        return templates.render("index.html", StandardModel(injector))
    }

    @Post
    @RouteLocation(RemoveLocation::class)
    suspend fun postIndex(@Post @MaxLength(1024) item: String) {
        Todo.CachedRepository.insert(Todo(Todo.Id(), item))
        redirect("/")
    }

    @Get
    suspend fun remove(location: RemoveLocation) {
        Todo.CachedRepository.delete(Todo.Id(location.id))
        redirect("/")
    }
}
*/

@Suppress("unused")
open class StandardModel(val injector: Injector) {
    suspend fun allTodos() = Todo.CachedRepository.getAll()
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

    class Id(id: String = randomUUID()) : AbstractId(id)

    companion object {
        val Repository = DbModelRepository(Todo::id)
        val CachedRepository = Repository.cached(MemoryCache())
    }
}
