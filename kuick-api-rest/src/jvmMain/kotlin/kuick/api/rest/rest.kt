package kuick.api.rest


import com.google.inject.Injector
import io.ktor.application.call
import io.ktor.http.ContentType
import io.ktor.http.HttpMethod
import io.ktor.request.receiveText
import io.ktor.response.respondText
import io.ktor.routing.Route
import io.ktor.routing.route
import io.ktor.util.AttributeKey
import kuick.api.buildArgsFromObject
import kuick.api.getAsTree
import kuick.api.rest.parameters.include.IncludeParam
import kuick.api.rest.parameters.include.includeRelatedResources
import kuick.api.rest.parameters.preserve.FieldsParam
import kuick.api.rest.parameters.preserve.preserveFields
import kuick.json.Json.gson
import kuick.json.Json.jsonParser
import java.lang.reflect.ParameterizedType
import java.lang.reflect.Type
import kotlin.reflect.KFunction
import kotlin.reflect.KProperty
import kotlin.reflect.full.callSuspend

abstract class TypeReference<T> : Comparable<TypeReference<T>> {
    val type: Type =
            (javaClass.genericSuperclass as ParameterizedType).actualTypeArguments[0]

    override fun compareTo(other: TypeReference<T>) = 0
}


data class RestRouting(
        val parent: Route,
        val resourceName: String,
        val api: Any,
        val injector: Injector
) {

    inline fun <reified T : Any?, R : Any?> registerRoute(route: RestRoute<R>): Route =
            parent.route(resourceName, method = route.httpMethod) {
                println("REST: ${route.httpMethod.value} /$resourceName -> ${route.handler}") // logging

                handle {

                    val args = buildArgsFromObject(
                            route.handler,
                            jsonParser.parse(call.receiveText()),
                            call.attributes.allKeys.map { it.name to call.attributes[it as AttributeKey<Any>] }.toMap()
                    )

                    val result = route.handler.callSuspend(api, *args.toTypedArray())

                    val jsonResult = gson.toJsonTree(result)

                    val queryParameters = call.request.queryParameters

                    //TODO handle case when we include smth just to cut it out when filtering
                    if (route.config.includeParameterConfiguration != null && "\$include" in queryParameters) {
                        val configuration = route.config.includeParameterConfiguration!!
                        val includeParam = IncludeParam.create(queryParameters.getAsTree("\$include"), T::class.java, configuration)
                        jsonResult.includeRelatedResources(includeParam, configuration)
                    }

                    if (route.config.withFieldsParameter && "\$fields" in queryParameters) {
                        val fieldsParam = FieldsParam.create(queryParameters.getAsTree("\$fields"), T::class.java)
                        jsonResult.preserveFields(fieldsParam)
                    }

                    call.respondText(jsonResult.toString(), ContentType.Application.Json) // serialization
                }
            }
}

class RestRoute<T>(
        val httpMethod: HttpMethod,
        val handler: KFunction<T>,
        val config: Configuration = Configuration()
) {

    class Configuration {
        var withFieldsParameter: Boolean = false
            private set
        var includeParameterConfiguration: Map<String, suspend (id: String) -> Any>? = null
            private set

        fun withFieldsParameter() {
            withFieldsParameter = true
        }

        fun withIncludeParameter(vararg configuration: Pair<KProperty<Any?>, suspend (id: String) -> Any>) {
            includeParameterConfiguration = configuration
                    .map { it.first.name to it.second }.toMap()
        }
    }

}


