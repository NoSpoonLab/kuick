package kuick.samples.todo2.infrastructure

import com.google.gson.Gson
import com.google.gson.GsonBuilder
import io.ktor.application.Application
import io.ktor.routing.Routing
import io.ktor.routing.routing


class KuickRouting(val routing: Routing)

fun Application.kuickRouting(configuration: KuickRouting.() -> Unit): KuickRouting =
        KuickRouting(routing { }).apply(configuration)

val gson: Gson = GsonBuilder().create()
