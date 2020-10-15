package kuick.logging

import kotlin.js.*

private val loggers = LinkedHashMap<String, Logger>()
fun Logger(name: String): Logger {
    logInitOnce()
    return loggers.getOrPut(name) { LogCreate(name) }
}

fun LoggerInit() = logInitOnce()
fun LoggerGetLoggers() = loggers.values.toList()

private var logInitialized = false
private fun logInitOnce() {
    if (logInitialized) return
    logInitialized = true
    LogInit()
}
internal expect fun LogInit()
internal expect fun LogCreate(name: String): Logger

object LoggerConfig {
    init {
        logInitOnce()
    }

    class ItemConfig(val logger: Logger) {
        var enabled: Boolean = true
        var enabledTags: MutableSet<String>? = null
        var disabledTags: MutableSet<String>? = null
        var minLevel: LogLevel? = null

        fun enableTag(tag: String) {
            if (enabledTags == null) enabledTags = LinkedHashSet()
            enabledTags!!.add(tag)
        }

        fun disableTag(tag: String) {
            if (disabledTags == null) disabledTags = LinkedHashSet()
            disabledTags!!.add(tag)
        }
    }

    var enabled: Boolean = true
    var defaultMinLevel: LogLevel = LogLevel.WARN
    private val perLog = LinkedHashMap<Logger, ItemConfig>()

    fun getConfig(logger: Logger) = perLog.getOrPut(logger) { ItemConfig(logger) }

    fun enabled(logger: Logger, level: LogLevel, tag: String?): Boolean {
        if (!enabled) return false
        val config = getConfig(logger)
        val configMinLevel = config.minLevel ?: defaultMinLevel
        if (level.index > configMinLevel.index) return false

        val enabledTags = config.enabledTags
        val disabledTags = config.disabledTags
        if (disabledTags != null && tag in disabledTags) return false
        if (enabledTags != null && tag !in enabledTags) return false

        return true
    }
}

enum class LogLevel(val index: Int) {
    FATAL(0), ERROR(1), WARN(2), INFO(3), TRACE(4);

    companion object {
        private val BY_NAME = values().associateBy { it.name }
        operator fun invoke(name: String): LogLevel? = BY_NAME[name.toUpperCase()]
    }
}

interface Logger {
    @JsName("name")
    val name: String
    fun enabled(level: LogLevel, tag: String? = null): Boolean = LoggerConfig.enabled(this, level, tag)
    fun log(level: LogLevel, message: String, tag: String?)
}

val Logger.config get() = LoggerConfig.getConfig(this)

inline fun Logger.log(level: LogLevel, tag: String? = null, message: () -> String) {
    if (enabled(level, tag)) log(level, message(), tag)
}

inline fun Logger.trace(tag: String? = null, message: () -> String) = log(LogLevel.TRACE, tag, message)
inline fun Logger.info(tag: String? = null, message: () -> String) = log(LogLevel.INFO, tag, message)
inline fun Logger.warn(tag: String? = null, message: () -> String) = log(LogLevel.WARN, tag, message)
inline fun Logger.error(tag: String? = null, message: () -> String) = log(LogLevel.ERROR, tag, message)
inline fun Logger.fatal(tag: String? = null, message: () -> String) = log(LogLevel.ERROR, tag, message)
