package kuick.env

import java.io.File
import java.util.*


open class BaseEnvironment(val envs: MutableMap<String, String> = System.getenv().toMutableMap()) : MutableMap<String, String> by envs {
    @Deprecated("", ReplaceWith("this[key]"))
    operator fun invoke(key: String) = this[key]

    fun describe() {
        println("ENVIRONMENT ------")
        toList().sortedBy { it.first }.forEach { (t, u) -> println("$t: $u") }
        println("/ENVIRONMENT ------")
    }


    fun env(name: String, default: String? = null) =
        this[name]
            ?: default
            ?: error("Can't find environment '$name'")

    fun load(props: Properties) {
        for (prop in props) {
            this[prop.key.toString()] = prop.value.toString()
        }
    }

    fun load(file: File) {
        if (!file.exists()) error("File $file doesn't exists")
        load(Properties().apply { file.inputStream().use { file -> this.load(file) } })
    }
}

object Environment : BaseEnvironment()
