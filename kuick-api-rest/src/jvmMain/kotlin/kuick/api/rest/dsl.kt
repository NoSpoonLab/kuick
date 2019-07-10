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

inline fun <reified T, R> RestRouting.route(
        httpMethod: HttpMethod,
        handler: KFunction<R>,
        config: RestRoute.Configuration.() -> Unit = {}
)
        : RestRoute<R> =
        RestRoute(httpMethod, handler)
                .apply { config(this.config) }
                .also { registerRoute<T, R>(it) }


inline fun <reified T> RestRouting.getMany(
        handler: KFunction<List<T>>,
        config: RestRoute.Configuration.() -> Unit = {}
): RestRoute<List<T>> =
        route<T, List<T>>(HttpMethod.Get, handler, config)

inline fun <reified T> RestRouting.get(
        handler: KFunction<T>,
        config: RestRoute.Configuration.() -> Unit = {}
): RestRoute<T> =
        route<T, T>(HttpMethod.Get, handler, config)

inline fun <reified T> RestRouting.put(
        handler: KFunction<T>,
        config: RestRoute.Configuration.() -> Unit = {}
): RestRoute<T> =
        route<T, T>(HttpMethod.Put, handler, config)

inline fun <reified T> RestRouting.post(
        handler: KFunction<T>,
        config: RestRoute.Configuration.() -> Unit = {}
): RestRoute<T> =
        route<T, T>(HttpMethod.Post, handler, config)

inline fun <reified T> RestRouting.delete(
        handler: KFunction<T>,
        config: RestRoute.Configuration.() -> Unit = {}
): RestRoute<T> =
        route<T, T>(HttpMethod.Delete, handler, config)

inline fun <reified T> RestRouting.patch(
        handler: KFunction<T>,
        config: RestRoute.Configuration.() -> Unit = {}
): RestRoute<T> =
        route<T, T>(HttpMethod.Patch, handler, config)
