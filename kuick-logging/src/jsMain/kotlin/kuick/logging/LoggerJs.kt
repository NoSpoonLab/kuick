package kuick.logging

import kotlin.browser.*

internal actual fun LogCreate(name: String): Logger = object : Logger {
    override val name: String = name

    override fun log(level: LogLevel, message: String, tag: String?) {
        when (level) {
            LogLevel.TRACE, LogLevel.INFO -> console.log(message, tag)
            LogLevel.WARN -> console.warn(message, tag)
            LogLevel.ERROR, LogLevel.FATAL -> console.error(message, tag)
        }
    }
}

@Suppress("unused")
object LogJs {
    val loggers get() = run {
        val out = js("([])")
        for (logger in LoggerGetLoggers()) out.push(logger.name)
        out
    }

    @JsName("enableTag")
    fun enableTag(name: String, tag: String) = Logger(name).config.enableTag(tag)

    @JsName("disableTag")
    fun disableTag(name: String, tag: String) = Logger(name).config.disableTag(tag)

    @JsName("setMinLevel")
    fun setMinLevel(name: String, level: String) = run { Logger(name).config.minLevel = LogLevel(level) }
}

internal actual fun LogInit() {
    // Exposes LogJs as log to javascript
    window.asDynamic().log = LogJs
}
