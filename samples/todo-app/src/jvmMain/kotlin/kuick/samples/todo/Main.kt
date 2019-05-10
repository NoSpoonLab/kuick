package kuick.samples.todo

import com.google.inject.Injector
import com.soywiz.korio.file.std.resourcesVfs
import com.soywiz.korte.Templates
import com.soywiz.korte.ktor.Korte
import io.ktor.application.Application
import io.ktor.application.install
import io.ktor.http.ContentType
import io.ktor.server.engine.embeddedServer
import io.ktor.server.netty.Netty
import kuick.client.db.DbClientPool
import kuick.client.jdbc.JdbcDriver
import kuick.client.repositories.DbModelRepository
import kuick.di.Guice
import kuick.di.bindPerCoroutineJob
import kuick.ktor.*
import kuick.models.AbstractId
import kuick.repositories.annotations.MaxLength
import kuick.repositories.patterns.MemoryCache
import kuick.repositories.patterns.cached
import kuick.utils.randomUUID

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

@Suppress("unused")
open class StandardModel(val injector: Injector) {
    suspend fun allTodos() = Todo.CachedRepository.getAll()
}

data class Todo(
        val id: Id,
        @MaxLength(512) val text: String
) {
    @Suppress("unused")
    fun removeLink() = "/remove/$id"

    class Id(id: String = randomUUID()) : AbstractId(id)

    companion object {
        val Repository = DbModelRepository(Todo::id)
        val CachedRepository = Repository.cached(MemoryCache())
    }
}
