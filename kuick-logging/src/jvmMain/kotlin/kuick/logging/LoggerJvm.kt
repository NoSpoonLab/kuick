package kuick.logging

import java.io.*
import java.text.*
import java.util.*
import java.text.SimpleDateFormat

internal actual fun LogCreate(name: String): Logger = object : Logger {
    override val name: String = name
    private val dateFormat = SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS")

    override fun log(level: LogLevel, message: String, tag: String?) {
        if (!this.enabled(level, tag)) return
        val date = dateFormat.format(Date())
        val printStream = when (level) {
            LogLevel.ERROR, LogLevel.FATAL -> System.err
            else -> System.out
        }
        val message2 = if (tag != null) "[$tag] $message" else message
        printStream.println("$date [$name] [$level] $message2")
    }
}

internal actual fun LogInit() {
    val file = File("kuick-logging.properties").absoluteFile
    println("Trying to load logging configuration: $file")
    try {
        if (file.exists()) {
            val props = Properties().also { props -> props.load(file.readText().reader()) }.map {
                it.key.toString() to it.value.toString()
            }.toMap()
            for ((key, value) in props) {
                val level = LogLevel(value)
                Logger(key).config.minLevel = level
                println(" - Logger(\"$key\").config.minLevel = $level ($value)")
            }
        }
    } catch (e: Throwable) {
        e.printStackTrace()
    }
}
