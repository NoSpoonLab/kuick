package kuick.api.rest

import com.google.inject.Injector
import io.ktor.http.HttpMethod
import io.ktor.routing.Route
import kuick.ktor.KuickRouting
import kotlin.reflect.KFunction


inline fun <reified T> KuickRouting.restRouting(
        injector: Injector,
        resourceName: String,
        configuration: RestRouting.() -> Unit
) {
    val api = injector.getInstance(T::class.java)!!
    configuration(
            RestRouting(this, resourceName, api, injector)
    )
}


fun <T> RestRouting.get(handler: KFunction<T>, configuration: RestRouting.() -> Unit = {}): Route = route(HttpMethod.Get, handler, configuration)
fun <T> RestRouting.put(handler: KFunction<T>, configuration: RestRouting.() -> Unit = {}): Route = route(HttpMethod.Put, handler, configuration)
fun <T> RestRouting.post(handler: KFunction<T>, configuration: RestRouting.() -> Unit = {}): Route = route(HttpMethod.Post, handler, configuration)
fun <T> RestRouting.delete(handler: KFunction<T>, configuration: RestRouting.() -> Unit = {}): Route = route(HttpMethod.Delete, handler, configuration)
fun <T> RestRouting.patch(handler: KFunction<T>, configuration: RestRouting.() -> Unit = {}): Route = route(HttpMethod.Patch, handler, configuration)
