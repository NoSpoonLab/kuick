package kuick.json

import com.google.gson.*
import org.intellij.lang.annotations.Language
import java.lang.reflect.Type
import java.text.SimpleDateFormat
import java.time.*
import java.time.format.DateTimeFormatter
import java.util.*
import kotlin.reflect.KClass
import kotlin.reflect.KType
import kotlin.reflect.jvm.javaType


class DateAdapter : JsonDeserializer<Date>, JsonSerializer<Date> {

    val sdf = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssX")

    override fun deserialize(je: JsonElement, type: Type, ctx: JsonDeserializationContext): Date {
        val dateVal = (je as JsonPrimitive)
        if (dateVal.isNumber) {
            return Date(dateVal.asLong)
        } else {
            try {
                return sdf.parse(dateVal.asString)
            } catch (e: Exception) {
                return Date(LocalDateTime.parse(dateVal.asString).toInstant(ZoneOffset.UTC).toEpochMilli())
            }
        }
    }

    override fun serialize(date: Date, type: Type, ctx: JsonSerializationContext): JsonElement {
        return JsonPrimitive(date.time)
    }

}

class InvalidLocalDateTimeFormat(msg: String) : Throwable(msg)

private val REG_EXP_DATE_FORMAT = "\\+?\\d+-\\d{2}-\\d{2}T\\d{2}:\\d{2}"

fun serializeLocalDateTime(ldt: LocalDateTime): String {
    val date = if (ldt == LocalDateTime.MAX) LocalDateTime.of(9999, 12, 31, 23, 59, 59)
    else ldt
    val match = Regex(REG_EXP_DATE_FORMAT).find(date.toString())
    match ?: throw InvalidLocalDateTimeFormat("Format [$ldt] for LocalDateTime is invalid")

    return match.value
}

fun unserializeLocalDateTime(s: String): LocalDateTime {
    val match = Regex(REG_EXP_DATE_FORMAT).find(s)
    match ?: throw InvalidLocalDateTimeFormat("Format [$s] for LocalDateTime is invalid")

    return LocalDateTime.parse(match.value)
}

class LocalDateTimeAdapter : JsonDeserializer<LocalDateTime>, JsonSerializer<LocalDateTime> {

    override fun deserialize(je: JsonElement, type: Type, ctx: JsonDeserializationContext): LocalDateTime {
        val dateVal = (je as JsonPrimitive)
        if (dateVal.isNumber) {
            return LocalDateTime.ofInstant(Instant.ofEpochMilli(dateVal.asLong), ZoneId.of("GMT+1")).withSecond(0).withNano(0)
        } else {
            return unserializeLocalDateTime(dateVal.asString)
        }
    }

    override fun serialize(date: LocalDateTime, type: Type, ctx: JsonSerializationContext): JsonElement {
        return JsonPrimitive(serializeLocalDateTime(date))
    }

}

class LocalTimeAdapter : JsonDeserializer<LocalTime>, JsonSerializer<LocalTime> {
    override fun deserialize(je: JsonElement, type: Type?, ctx: JsonDeserializationContext?): LocalTime {
        val value = (je as JsonPrimitive).asString
        if (value.matches(Regex("\\d\\d:\\d\\d"))) {
            return LocalTime.parse(value + ":00")
        }
        return LocalTime.parse(value)
    }

    override fun serialize(time: LocalTime, type: Type?, ctz: JsonSerializationContext?): JsonElement {
        return JsonPrimitive(time.format(DateTimeFormatter.ISO_TIME))
    }
}

object Json {

    val jsonParser: JsonParser = JsonParser()

    val gson: Gson = GsonBuilder()
            //.registerTypeHierarchyAdapter(Id::class.java, IdGsonAdapter())
            //.registerTypeAdapter(CounterNumber::class.java, CounterNumberGsonAdapter())
            .registerTypeAdapter(Date::class.java, DateAdapter())
            .registerTypeAdapter(LocalDateTime::class.java, LocalDateTimeAdapter())
            .registerTypeAdapter(LocalTime::class.java, LocalTimeAdapter())
            .create()

    fun <T : Any> toJson(any: T): String = gson.toJson(any)

    fun <T : Any> fromJson(@Language("JSON") json: String, clazz: Type): T {
        try {
            return gson.fromJson(json, clazz)
        } catch (t: Throwable) {
            println("ERROR parsing JSON: $clazz <-- $json")
            throw t
        }
    }

    fun <T : Any> fromJson(json: String, clazz: KClass<T>): T {
        try {
            return gson.fromJson(json, clazz.java)
        } catch (t: Throwable) {
            println("ERROR parsing JSON: $clazz <-- $json")
            throw t
        }
    }

    fun <T : Any> fromJson(json: String, type: KType): T {
        try {
            return gson.fromJson(json, type.javaType)
        } catch (t: Throwable) {
            println("ERROR parsing JSON: $type <-- $json")
            throw t
        }
    }

    inline fun <reified T : Any> fromJson(json: String): T {
        try {
            return gson.fromJson(json, T::class.java)
        } catch (t: Throwable) {
            println("ERROR parsing JSON: ${T::class.java} <-- $json")
            throw t
        }
    }
}

fun annotateJson(@Language("JSON") json: String): String = json