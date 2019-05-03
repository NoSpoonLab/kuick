package kuick.api.rest

import com.google.inject.Injector
import io.ktor.http.HttpMethod
import io.ktor.routing.Route
import kotlin.reflect.KFunction


inline fun <reified T> Route.restRoute(
        injector: Injector,
        resourceName: String,
        config: RestRouting.() -> Unit = {}
): RestRouting {
    val api = injector.getInstance(T::class.java)!!
    return RestRouting(this, resourceName, api, injector).apply(config)
}

fun <T> RestRouting.route(
        httpMethod: HttpMethod,
        handler: KFunction<T>,
        config: RestRoute.Configuration.() -> Unit = {}
)
        : RestRoute<T> =
        RestRoute(httpMethod, handler)
                .apply { config(this.config) }
                .also { registerRoute(it) }

fun <T> RestRouting.get(
        handler: KFunction<T>,
        config: RestRoute.Configuration.() -> Unit = {}
): RestRoute<T> =
        route(HttpMethod.Get, handler, config)

fun <T> RestRouting.put(
        handler: KFunction<T>,
        config: RestRoute.Configuration.() -> Unit = {}
): RestRoute<T> =
        route(HttpMethod.Put, handler, config)

fun <T> RestRouting.post(
        handler: KFunction<T>,
        config: RestRoute.Configuration.() -> Unit = {}
): RestRoute<T> =
        route(HttpMethod.Post, handler, config)

fun <T> RestRouting.delete(
        handler: KFunction<T>,
        config: RestRoute.Configuration.() -> Unit = {}
): RestRoute<T> =
        route(HttpMethod.Delete, handler, config)

fun <T> RestRouting.patch(
        handler: KFunction<T>,
        config: RestRoute.Configuration.() -> Unit = {}
): RestRoute<T> =
        route(HttpMethod.Patch, handler, config)
