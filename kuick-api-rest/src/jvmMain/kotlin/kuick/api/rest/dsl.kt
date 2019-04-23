package kuick.api.rest

import com.google.inject.Injector
import io.ktor.http.HttpMethod
import io.ktor.routing.Route
import kotlin.reflect.KFunction


inline fun <reified T> Route.restRoute(
        injector: Injector,
        resourceName: String,
        configuration: RestRouting.() -> Unit
): RestRouting {
    val api = injector.getInstance(T::class.java)!!
    return RestRouting(this, resourceName, api, injector).apply(configuration)
}

fun <T> RestRouting.route(
        httpMethod: HttpMethod,
        handler: KFunction<T>,
        configuration: RestRoute<T>.() -> Unit = {}
)
        : RestRoute<T> =
        RestRoute(httpMethod, handler)
                .apply(configuration)
                .also { registerRoute(it) }

fun <T> RestRouting.get(handler: KFunction<T>, configuration: RestRoute<T>.() -> Unit = {}): RestRoute<T> =
        route(HttpMethod.Get, handler, configuration)

fun <T> RestRouting.put(handler: KFunction<T>, configuration: RestRoute<T>.() -> Unit = {}): RestRoute<T> =
        route(HttpMethod.Put, handler, configuration)

fun <T> RestRouting.post(handler: KFunction<T>, configuration: RestRoute<T>.() -> Unit = {}): RestRoute<T> =
        route(HttpMethod.Post, handler, configuration)

fun <T> RestRouting.delete(handler: KFunction<T>, configuration: RestRoute<T>.() -> Unit = {}): RestRoute<T> =
        route(HttpMethod.Delete, handler, configuration)

fun <T> RestRouting.patch(handler: KFunction<T>, configuration: RestRoute<T>.() -> Unit = {}): RestRoute<T> =
        route(HttpMethod.Patch, handler, configuration)
