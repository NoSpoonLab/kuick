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

inline fun RestRouting.route(
        httpMethod: HttpMethod,
        handler: KFunction<*>,
        config: RestRoute.Configuration.() -> Unit = {}
)
        : RestRoute =
        RestRoute(httpMethod, handler)
                .apply { config(this.config) }
                .also { registerRoute(it) }


inline fun RestRouting.get(
        handler: KFunction<*>,
        config: RestRoute.Configuration.() -> Unit = {}
): RestRoute =
        route(HttpMethod.Get, handler, config)

inline fun RestRouting.put(
        handler: KFunction<*>,
        config: RestRoute.Configuration.() -> Unit = {}
): RestRoute =
        route(HttpMethod.Put, handler, config)

inline fun RestRouting.post(
        handler: KFunction<*>,
        config: RestRoute.Configuration.() -> Unit = {}
): RestRoute =
        route(HttpMethod.Post, handler, config)

inline fun RestRouting.delete(
        handler: KFunction<*>,
        config: RestRoute.Configuration.() -> Unit = {}
): RestRoute =
        route(HttpMethod.Delete, handler, config)

inline fun RestRouting.patch(
        handler: KFunction<*>,
        config: RestRoute.Configuration.() -> Unit = {}
): RestRoute =
        route(HttpMethod.Patch, handler, config)
