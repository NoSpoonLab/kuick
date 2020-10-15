package kuick.client.redis

import io.lettuce.core.api.async.*
import kotlin.reflect.*
import kotlin.reflect.full.*

// @TODO: Extract argument names from
object RedisGen {

    fun KType.cleanTypeStr() = toString().replace("!", "").replace("(out) ", "out ").replace("(Mutable)", "Mutable")

    @JvmStatic
    fun main(args: Array<String>) {
        //for (method in RedisAsyncCommands::class.java.methods) println("func: $method")
        for (method in RedisAsyncCommands::class.functions.sortedBy { it.name }) {
            val params = method.parameters.drop(1)
            if (method.name == "equals") continue
            if (method.name == "hashCode") continue
            if (method.name == "toString") continue

            val withT = when (method.name) {
                "dispatch", "eval", "evalsha" -> true
                else -> false
            }

            val line = buildString {
                append("suspend fun ")
                if (withT) {
                    append("<T> ")
                }
                append(method.name)
                append("(")
                append(params.joinToString(", ") {
                    val cleanType = it.type.cleanTypeStr()
                    if (it.isVararg) {
                        val fcleanType = when {
                            cleanType.startsWith("kotlin.Array<out ") -> cleanType.removePrefix("kotlin.Array<out ").removeSuffix(">")
                            cleanType == "kotlin.IntArray" -> "Int"
                            else -> cleanType
                        }
                        "vararg ${it.name}: $fcleanType"
                    } else {
                        "${it.name}: $cleanType"
                    }
                })
                append(")")
                append(" = async.")
                append(method.name)
                if (withT) {
                    append("<T>")
                }
                append("(")
                append(params.joinToString(", ") {
                    if (it.isVararg) "*${it.name}" else "${it.name}"
                })
                append(")")

                val returnTypeString = method.returnType.toString()
                //println("returnTypeString: $returnTypeString")
                if (returnTypeString.startsWith("io.lettuce.core.RedisFuture")) {
                    append(".await()")
                }
            }
            //method.parameters.first().name
            println(line)
        }
    }
}