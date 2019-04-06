package kuick.repositories.squash

import kuick.json.*
import kuick.models.*
import kuick.repositories.squash.orm.*
import org.jetbrains.squash.definition.*
import org.jetbrains.squash.results.*
import java.lang.reflect.*
import java.time.*
import java.util.*
import kotlin.reflect.*
import kotlin.reflect.full.*

class VarCharSerializationStrategy<T : Any>(
        clazz: KClass<T>,
        val varcharLength: Int,
        private val decodeValue: (targetType: KClass<T>, getValue: GetTypedValueFunc) -> T?,
        private val encodeValue: (value: Any) -> Any?
) : TypedSerializationStrategy<T>(clazz) {

    override fun getColumnDefinition(table: TableDefinition, info: PropertyInfo<*>): ColumnDefinition<Any?> = table.varchar(info.columnName, info.maxLength ?: varcharLength)
    override fun decodeValue(targetType: KClass<T>, getValue: (KClass<*>) -> Any?): T? = decodeValue.invoke(targetType, getValue)
    override fun encodeValue(value: Any): Any? = encodeValue.invoke(value)
    fun withLength(length: Int) = VarCharSerializationStrategy(clazz, length, decodeValue, encodeValue)
}

val dateSerializationAsDateTime = SerializationStrategy(
        { table, info -> table.datetime(info.columnName) },
        { targetType, getValue ->
            val value = getValue<LocalDateTime>()
            value?.let {
                val zoneOffset = ZoneOffset.UTC.normalized().rules.getOffset(it)
                Date.from(it.toInstant(zoneOffset))
            }
        },
        { value -> LocalDateTime.ofInstant((value as Date).toInstant(), ZoneOffset.UTC.normalized()) }
)

val dateSerializationAsLong = SerializationStrategy(
        { table, info -> table.long(info.columnName) },
        { targetType, getValue -> getValue<Long>()?.let { Date(it) } },
        { value -> (value as Date).time }
)

val stringSerialization = VarCharSerializationStrategy(
        String::class,
        LONG_TEXT_LEN,
        { targetType, getValue -> getValue<String>() },
        { value -> value })

val intSerialization = SerializationStrategy(
        { table, info -> table.integer(info.columnName) },
        { targetType, getValue -> getValue<Int>() },
        { value -> value }
)

val longSerialization = SerializationStrategy(
        { table, info -> table.long(info.columnName) },
        { targetType, getValue -> getValue<Long>() },
        { value -> value }
)

val doubleSerialization = SerializationStrategy(
        { table, info -> table.decimal(info.columnName, 5, 4) },
        { targetType, getValue -> getValue<Double>() },
        { value -> value }
)

val booleanSerialization = SerializationStrategy(
        { table, info -> table.bool(info.columnName) },
        { targetType, getValue -> getValue<Boolean>() },
        { value -> value }
)

val localDateSerialization = VarCharSerializationStrategy(
        LocalDate::class,
        LOCAL_DATE_TIME_LEN,
        { _, getValue ->
            getValue<String>()?.takeIf { it != "0000-00-00" }?.let { dateAsStr ->
                ignoreErrors { LocalDate.parse(dateAsStr, DATE_FORMAT) }
                        ?: ignoreErrors { LocalDate.parse(Json.fromJson<LocalDate>(dateAsStr).toString(), DATE_FORMAT) }
                        ?: error("Unknown date format [$dateAsStr]")
            }
        },
        { value -> DATE_FORMAT.format((value as LocalDate)) }
)

val localDateTimeSerialization = VarCharSerializationStrategy(
        LocalDateTime::class,
        LOCAL_DATE_TIME_LEN,
        { _, getValue ->
            getValue<String>()?.let { LocalDateTime.parse(it, DATE_TIME_FORMAT) }
        },
        { value -> DATE_TIME_FORMAT.format(value as LocalDateTime) }
)

val emailSerialization = VarCharSerializationStrategy(
        Email::class,
        LONG_TEXT_LEN,
        { _, getValue -> getValue<String>()?.let { Email(it) } },
        { value -> (value as Email).email }
)

object IdSerializationStrategy : SerializationStrategy {
    val idSerialization = VarCharSerializationStrategy(
            Id::class,
            ID_LEN,
            { clazz, getValue -> getValue<String>()?.let { clazz.primaryConstructor?.call(it) } },
            { value -> (value as Id).id }
    )

    override fun tryGetColumnDefinition(table: TableDefinition, info: PropertyInfo<*>) = when {
        info.returnType.isSubtypeOf(Id::class.starProjectedType) -> idSerialization.getColumnDefinition(table, info)
        else -> null
    }

    override fun tryDecodeValueLazy(targetType: KClass<*>, getValue: GetTypedValueFunc): Any? = when {
        targetType.isSubclassOf(Id::class) -> idSerialization.decodeValue(targetType as KClass<Id>, getValue)
        else -> SerializationStrategy.Unhandled
    }

    override fun tryEncodeValue(value: Any): Any? = when (value) {
        is Id -> idSerialization.encodeValue(value)
        else -> SerializationStrategy.Unhandled
    }
}

object JsonSerializationStrategy : SerializationStrategy {
    override fun tryGetColumnDefinition(table: TableDefinition, info: PropertyInfo<*>): ColumnDefinition<*>? =
            table.varchar(info.columnName, info.maxLength ?: LONG_TEXT_LEN)

    override fun tryDecodeValueLazy(targetType: KClass<*>, getValue: GetTypedValueFunc): Any? =
            getValue<String>()?.let { Json.fromJson(it, targetType) }

    override fun tryEncodeValue(value: Any): Any? = Json.toJson(value)

}

fun serializationStrategies(vararg serializations: SerializationStrategy): SerializationStrategy {
    if (serializations.isEmpty()) return SerializationStrategies()
    return serializations.drop(1).fold(serializations.first()) { l, r -> l + r }
}

val defaultSerializationStrategies: SerializationStrategy = serializationStrategies(
    intSerialization,
    longSerialization,
    stringSerialization,
    dateSerializationAsLong,
    doubleSerialization,
    booleanSerialization,
    localDateSerialization,
    localDateTimeSerialization,
    emailSerialization,
    IdSerializationStrategy
)
