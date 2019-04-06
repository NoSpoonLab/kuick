package kuick.repositories.squash

import kuick.json.*
import kuick.models.*
import kuick.repositories.annotations.*
import kuick.repositories.squash.orm.*
import org.jetbrains.squash.definition.*
import org.jetbrains.squash.results.*
import java.lang.reflect.*
import java.time.*
import java.util.*
import kotlin.reflect.*
import kotlin.reflect.full.*
import kotlin.reflect.jvm.*

class PropertyInfo<T>(val prop: KProperty<T>) {
    val maxLength = prop.javaField?.getAnnotation(MaxLength::class.java)?.maxLength
    val nullableProp = prop.returnType.isMarkedNullable
    val returnType = prop.returnType.classifier!!.starProjectedType
    val columnName = prop.name.toSnakeCase()

    private fun String.toSnakeCase(): String = flatMap {
        if (it.isUpperCase()) listOf('_', it.toLowerCase()) else listOf(it)
    }.joinToString("")
}

interface BaseSerializationStrategy {
    fun tryGetColumnDefinition(table: TableDefinition, prop: PropertyInfo<*>): ColumnDefinition<*>?
}

open class SerializationStrategy<T : Any>(
        val getColumnDefinition: (table: TableDefinition, columnName: String) -> ColumnDefinition<Any?>,
        val readColumnValue: (field: Field, resultRow: ResultRow, columnName: String, tableName: String) -> T?,
        val decodeValue: (value: Any) -> Any?
)

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
        { value -> LocalDateTime.ofInstant((value as Date).toInstant(), ZoneOffset.UTC.normalized()) }
)

val dateSerializationAsLong = SerializationStrategy<Date>(
        { table, columnName -> table.long(columnName) },
        { field, result, columnName, tableName -> result.columnValue<Long>(columnName, tableName)?.let { Date(it) } },
        { value -> (value as Date).time }
)


val stringSerialization = VarCharSerializationStrategy(
        LONG_TEXT_LEN,
        { field, result, columnName, tableName -> result.columnValue<String>(columnName, tableName) },
        { value -> value })

val intSerialization = SerializationStrategy(
        { table, columnName -> table.integer(columnName) },
        { field, result, columnName, tableName -> result.columnValue<Int>(columnName, tableName) },
        { value -> value }
)

val longSerialization = SerializationStrategy(
        { table, columnName -> table.long(columnName) },
        { type, result, columnName, tableName -> result.columnValue<Long>(columnName, tableName) },
        { value -> value }
)

val doubleSerialization = SerializationStrategy(
        { table, columnName -> table.decimal(columnName, 5, 4) },
        { field, result, columnName, tableName -> result.columnValue<Double>(columnName, tableName) },
        { value -> value }
)

val booleanSerialization = SerializationStrategy(
        { table, columnName -> table.bool(columnName) },
        { field, result, columnName, tableName -> result.columnValue<Boolean>(columnName, tableName) },
        { value -> value }
)

val localDateSerialization = VarCharSerializationStrategy(
        LOCAL_DATE_TIME_LEN,
        { field, result, columnName, tableName ->
            result.columnValue<String>(columnName, tableName)?.takeIf { it != "0000-00-00" }?.let { dateAsStr ->
                ignoreErrors { LocalDate.parse(dateAsStr, DATE_FORMAT) }
                        ?: ignoreErrors { LocalDate.parse(Json.fromJson<LocalDate>(dateAsStr).toString(), DATE_FORMAT) }
                        ?: error("Unknown date format [$dateAsStr]")
            }
        },
        { value -> DATE_FORMAT.format((value as LocalDate)) }
)


val localDateTimeSerialization = VarCharSerializationStrategy(
        LOCAL_DATE_TIME_LEN,
        { field, result, columnName, tableName ->
            result.columnValue<String>(columnName, tableName)?.let { LocalDateTime.parse(it, DATE_TIME_FORMAT) }
        },
        { value -> DATE_TIME_FORMAT.format(value as LocalDateTime) }
)

val idSerialization = VarCharSerializationStrategy(
        ID_LEN,
        { field, result, columnName, tableName -> result.columnValue<String>(columnName, tableName)?.let { field.type.kotlin.primaryConstructor?.call(it) as Id? } },
        { value -> (value as Id).id }
)


val emailSerialization = VarCharSerializationStrategy(
        LONG_TEXT_LEN,
        { field, result, columnName, tableName -> result.columnValue<String>(columnName, tableName)?.let { Email(it) } },
        { value -> (value as Email).email }
)

class SerializationStrategies(val strategies: Map<KType, SerializationStrategy<out Any>> = mapOf()) : BaseSerializationStrategy {
    override fun tryGetColumnDefinition(table: TableDefinition, prop: PropertyInfo<*>): ColumnDefinition<*>? {
        return strategies[prop.returnType]?.getColumnDefinition?.invoke(table, prop.columnName)
    }
}

class ComposableStrategies(val first: BaseSerializationStrategy, val second: BaseSerializationStrategy) : BaseSerializationStrategy {
    override fun tryGetColumnDefinition(table: TableDefinition, prop: PropertyInfo<*>): ColumnDefinition<*>? {
        return first.tryGetColumnDefinition(table, prop) ?: second.tryGetColumnDefinition(table, prop)
    }
}

fun <T : Any> BaseSerializationStrategy.withSerialization(clazz: KClass<T>, serialization: SerializationStrategy<T>) =
        if (this is SerializationStrategies) {
            SerializationStrategies(strategies + mapOf(clazz.starProjectedType to serialization))
        } else {
            ComposableStrategies(this, SerializationStrategies(mapOf(clazz.starProjectedType to serialization)))
        }

inline fun <reified T : Any> BaseSerializationStrategy.withSerialization(serialization: SerializationStrategy<T>) =
        withSerialization(T::class, serialization)


inline fun <reified T> type(): KType = T::class.starProjectedType

val defaultSerializationStrategies = SerializationStrategies(mapOf(
        type<Int>() to intSerialization,
        type<Long>() to longSerialization,
        type<String>() to stringSerialization,
        type<Date>() to dateSerializationAsLong,
        type<Double>() to doubleSerialization,
        type<Boolean>() to booleanSerialization,
        type<LocalDate>() to localDateSerialization,
        type<LocalDateTime>() to localDateTimeSerialization,
        type<Id>() to idSerialization,
        type<Email>() to emailSerialization)
)

