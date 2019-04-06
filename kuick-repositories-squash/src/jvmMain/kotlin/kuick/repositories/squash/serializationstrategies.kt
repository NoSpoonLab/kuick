package kuick.repositories.squash

import kuick.json.Json
import kuick.models.Email
import kuick.models.Id
import kuick.models.KLocalDate
import kuick.repositories.squash.orm.*
import org.jetbrains.squash.definition.*
import org.jetbrains.squash.results.ResultRow
import java.lang.reflect.Field
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.ZoneOffset
import java.util.*
import kotlin.reflect.KClass
import kotlin.reflect.KType
import kotlin.reflect.full.primaryConstructor
import kotlin.reflect.full.starProjectedType


open class SerializationStrategy<T : Any>(
        val getColumnDefinition: (table: TableDefinition, columnName: String) -> ColumnDefinition<Any?>,
        val readColumnValue: (field: Field, resultRow: ResultRow, columnName: String, tableName: String) -> T?,
        val decodeValue: (value: Any) -> Any?)

class VarCharSerializationStrategy<T : Any>(
        varcharLength: Int,
        readColumnValue: (Field, ResultRow, String, String) -> T?,
        decodeValue: (Any) -> Any?
) : SerializationStrategy<T>({ table, columnName -> table.varchar(columnName, varcharLength) }, readColumnValue, decodeValue) {
    fun withLength(length: Int) = VarCharSerializationStrategy(length, readColumnValue, decodeValue)
}


val dateSerializationAsDateTime = SerializationStrategy<Date>(
        { table, columnName -> table.datetime(columnName) },
        { type, result, columnName, tableName ->
            result.columnValue<LocalDateTime>(columnName, tableName)?.let {
                val zoneOffset = ZoneOffset.UTC.normalized().rules.getOffset(it)
                Date.from(it.toInstant(zoneOffset))
            }
        },
        { value -> LocalDateTime.ofInstant((value as Date).toInstant(), ZoneOffset.UTC.normalized()) })


val dateSerializationAsLong = SerializationStrategy<Date>(
        { table, columnName -> table.long(columnName) },
        { field, result, columnName, tableName -> result.columnValue<Long>(columnName, tableName)?.let { Date(it) } },
        { value -> (value as Date).time })


val stringSerialization = VarCharSerializationStrategy(
        LONG_TEXT_LEN,
        { field, result, columnName, tableName -> result.columnValue<String>(columnName, tableName) },
        { value -> value })

val intSerialization = SerializationStrategy(
        { table, columnName -> table.integer(columnName) },
        { field, result, columnName, tableName -> result.columnValue<Int>(columnName, tableName) },
        { value -> value })

val longSerialization = SerializationStrategy(
        { table, columnName -> table.long(columnName) },
        { type, result, columnName, tableName -> result.columnValue<Long>(columnName, tableName) },
        { value -> value })

val doubleSerialization = SerializationStrategy(
        { table, columnName -> table.decimal(columnName, 5, 4) },
        { field, result, columnName, tableName -> result.columnValue<Double>(columnName, tableName) },
        { value -> value })

val booleanSerialization = SerializationStrategy(
        { table, columnName -> table.bool(columnName) },
        { field, result, columnName, tableName -> result.columnValue<Boolean>(columnName, tableName) },
        { value -> value })

val localDateSerialization = VarCharSerializationStrategy(
        LOCAL_DATE_TIME_LEN,
        { field, result, columnName, tableName ->
            result.columnValue<String>(columnName, tableName)?.takeIf { it != "0000-00-00" }?.let { dateAsStr ->
                ignoreErrors { LocalDate.parse(dateAsStr, DATE_FORMAT) }
                        ?: ignoreErrors { LocalDate.parse(Json.fromJson<LocalDate>(dateAsStr).toString(), DATE_FORMAT) }
                        ?: error("Unknown date format [$dateAsStr]")
            }
        },
        { value -> DATE_FORMAT.format((value as LocalDate)) })


val localDateTimeSerialization = VarCharSerializationStrategy(
        LOCAL_DATE_TIME_LEN,
        { field, result, columnName, tableName ->
            result.columnValue<String>(columnName, tableName)?.let { LocalDateTime.parse(it, DATE_TIME_FORMAT) }
        },
        { value -> DATE_TIME_FORMAT.format(value as LocalDateTime) })

val idSerialization = VarCharSerializationStrategy(
        ID_LEN,
        { field, result, columnName, tableName -> result.columnValue<String>(columnName, tableName)?.let { field.type.kotlin.primaryConstructor?.call(it) as Id? } },
        { value -> (value as Id).id })


val emailSerialization = VarCharSerializationStrategy(
        LONG_TEXT_LEN,
        { field, result, columnName, tableName -> result.columnValue<String>(columnName, tableName)?.let { Email(it) }},
        { value -> (value as Email).email })


class SerializationStrategies(val strategies: Map<KType, SerializationStrategy<out Any>> ){

    fun withSerialization(clazz: KClass<*>,serialization: SerializationStrategy<out Any>) : SerializationStrategies{
        val newMap = strategies.toMutableMap()
        newMap[clazz.starProjectedType] = serialization
        return SerializationStrategies(newMap)
    }
}

inline fun <reified T> type() : KType = T::class.starProjectedType

val defaultSerializationStrategies  = SerializationStrategies(mapOf(
        Pair(type<Int>(),intSerialization),
        Pair(type<Long>(), longSerialization),
        Pair(type<String>(), stringSerialization),
        Pair(type<Date>(), dateSerializationAsLong),
        Pair(type<Double>(), doubleSerialization),
        Pair(type<Boolean>(), booleanSerialization),
        Pair(type<LocalDate>(), localDateSerialization),
        Pair(type<LocalDateTime>(), localDateTimeSerialization),
        Pair(type<Id>(), idSerialization),
        Pair(type<Email>(), emailSerialization)))

