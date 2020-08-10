package kuick.env

import java.lang.IllegalArgumentException

object Environment {

    fun describe() {
        println("ENVIRONMENT ------")
        System.getenv()
            .toList()
            .sortedBy { it.first }
            .forEach { (t, u) ->
                println("$t: $u")
            }
        println("/ENVIRONMENT ------")
    }

    fun env(key: String, default: String? = null): String =
        System.getenv(key)
            ?: default
            ?: throw IllegalArgumentException("Missing environment variable [$key]")

}
