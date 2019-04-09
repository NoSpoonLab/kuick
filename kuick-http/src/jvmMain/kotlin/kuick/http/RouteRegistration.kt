package kuick.http

/*
import kotlin.reflect.*
import kotlin.reflect.full.*

abstract class Location(val locationPath: String)

abstract class Parameters()

annotation class Route(val path: String)
annotation class RouteLocation(val location: KClass<out Location>, val method: HttpMethod = HttpMethod.ANY)

// Source of values
annotation class Param(val name: String = "")

annotation class Get(val name: String = "")

annotation class Post(val name: String = "")

suspend fun <T : Any> HttpServer.registerRoutesInClass(instance: T, clazz: KClass<out T> = instance::class) {
    val server = this
    for (func in clazz.declaredMemberFunctions) {
        val route = func.findAnnotation<Route>() ?: continue
        server.register(route.path, route.method) {
            val result = func.callSuspend(instance)
            SimpleHttpResponse(200, body = result ?: "")
        }
        println("$route : $func")
    }
}
*/