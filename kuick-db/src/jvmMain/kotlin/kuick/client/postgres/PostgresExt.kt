package kuick.client.postgres

import io.reactiverse.kotlin.pgclient.*
import io.reactiverse.kotlin.pgclient.PgClient
import io.reactiverse.pgclient.*
import io.reactiverse.pgclient.data.*
import io.vertx.core.*
import kotlinx.coroutines.*
import java.time.*
import kotlin.reflect.*
import kotlin.system.*

// https://hub.docker.com/_/postgres
// postgres://$username:$password@$host:$port/$database
// docker rm some-postgres; docker run --name some-postgres -e POSTGRES_PASSWORD= -p5432:5432 -d postgres
suspend fun PgClient.connect(vertx: Vertx, connectionUri: String = "postgres://postgres@localhost:5432/postgres"): PgConnection =
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

suspend fun <T> PgConnection.transaction(callback: suspend (PgTransaction) -> T): T {
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
    Long::class -> getLong(name) as T
    Numeric::class -> getNumeric(name) as T
    Float::class -> getFloat(name) as T
    Double::class -> getDouble(name) as T
    String::class -> getString(name) as T
    LocalDate::class -> getLocalDate(name) as T
    LocalTime::class -> getLocalTime(name) as T
    Json::class -> getJson(name) as T
    else -> getValue(name) as T
}

fun <T : Any> Row.get(clazz: KClass<T>, offset: Int): T = when (clazz) {
    Int::class -> getInteger(offset) as T
    Long::class -> getLong(offset) as T
    Numeric::class -> getNumeric(offset) as T
    Float::class -> getFloat(offset) as T
    Double::class -> getDouble(offset) as T
    String::class -> getString(offset) as T
    LocalDate::class -> getLocalDate(offset) as T
    LocalTime::class -> getLocalTime(offset) as T
    Json::class -> getJson(offset) as T
    else -> getValue(offset) as T
}

inline fun <reified T : Any> Row.get(name: String) = get(T::class, name)
inline fun <reified T : Any> Row.get(offset: Int) = get(T::class, offset)

suspend fun main() {
    //PgClient.connectAwait()
    Vertx.vertx().use { vertx ->
        PgClient.connect(vertx).use { client ->
            coroutineScope {
                val result = measureTimeMillis {
                    val a = launch {
                        client.transaction { transaction ->
                            delay(1000L)
                            for (item in transaction.queryAwait("SELECT 1;").toList()) {
                                println(item.get<Int>(0))
                            }
                        }
                    }
                    val b = launch {
                        client.transaction { transaction ->
                            delay(1000L)
                            for (item in transaction.queryAwait("SELECT 2;").toList()) {
                                println(item.get<Int>(0))
                            }
                        }
                    }
                    a.join()
                    b.join()
                }

                println("Time: $result")
            }
        }
    }
    Unit
}