package kuick.client.postgres

import io.reactiverse.kotlin.pgclient.*
import io.reactiverse.kotlin.pgclient.PgClient
import io.reactiverse.pgclient.*
import io.vertx.core.*
import kotlin.reflect.*

// https://hub.docker.com/_/postgres
// postgres://$username:$password@$host:$port/$database
// docker rm some-postgres; docker run --name some-postgres -e POSTGRES_PASSWORD= -p5432:5432 -d postgres
suspend fun PgClient.connect(vertx: Vertx, connectionUri: String = "postgres://postgres@localhost:5432/postgres") =
        this.connectAwait(vertx, connectionUri)

inline fun <T> PgConnection.use(callback: (PgConnection) -> T) {
    try {
        callback(this)
    } finally {
        close()
    }
}

inline fun <T> Vertx.use(callback: (Vertx) -> T) {
    try {
        callback(this)
    } finally {
        close()
    }
}

suspend fun <T> PgConnection.transaction(callback: suspend PgTransaction.() -> T): T {
    val transaction = begin()
    val result = try {
        callback(transaction)
    } catch (e: Throwable) {
        transaction.rollbackAwait()
        throw e
    }

    transaction.commitAwait()
    return result
}

fun <T : Any> Row.get(clazz: KClass<T>, name: String): T = when (clazz) {
    Int::class -> getInteger(name) as T
    else -> TODO()
}

fun <T : Any> Row.get(clazz: KClass<T>, offset: Int): T = when (clazz) {
    Int::class -> getInteger(offset) as T
    else -> TODO()
}

inline fun <reified T : Any> Row.get(name: String) = get(T::class, name)
inline fun <reified T : Any> Row.get(offset: Int) = get(T::class, offset)

suspend fun main() {
    //PgClient.connectAwait()
    Vertx.vertx().use { vertx ->
        PgClient.connect(vertx).use { client ->
            client.transaction {
                for (item in this.queryAwait("SELECT 1;").toList()) {
                    println(item.get<Int>(0))
                }
            }
        }
    }
}