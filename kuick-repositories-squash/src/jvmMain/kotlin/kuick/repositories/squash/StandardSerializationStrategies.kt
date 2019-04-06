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
        private val readColumnValue: (Field, ResultRow, String, String) -> T?,
        private val decodeValue: (Any) -> Any?
) : TypedSerializationStrategy<T>(clazz) {

    override fun getColumnDefinition(table: TableDefinition, info: PropertyInfo<*>): ColumnDefinition<Any?> =
            table.varchar(info.columnName, info.maxLength ?: varcharLength)

    override fun readColumnValue(field: Field, resultRow: ResultRow, columnName: String, tableName: String): T? =
            readColumnValue.invoke(field, resultRow, columnName, tableName)

    override fun decodeValue(value: Any): Any? = decodeValue.invoke(value)

    fun withLength(length: Int) = VarCharSerializationStrategy(clazz, length, readColumnValue, decodeValue)
}

val dateSerializationAsDateTime = SerializationStrategy(
        { table, info -> table.datetime(info.columnName) },
        { type, result, columnName, tableName ->
            result.columnValue<LocalDateTime>(columnName, tableName)?.let {
                val zoneOffset = ZoneOffset.UTC.normalized().rules.getOffset(it)
                Date.from(it.toInstant(zoneOffset))
            }
        },
        { value -> LocalDateTime.ofInstant((value as Date).toInstant(), ZoneOffset.UTC.normalized()) }
)

val dateSerializationAsLong = SerializationStrategy(
        { table, info -> table.long(info.columnName) },
        { field, result, columnName, tableName -> result.columnValue<Long>(columnName, tableName)?.let { Date(it) } },
        { value -> (value as Date).time }
)


val stringSerialization = VarCharSerializationStrategy(
        String::class,
        LONG_TEXT_LEN,
        { field, result, columnName, tableName -> result.columnValue<String>(columnName, tableName) },
        { value -> value })

val intSerialization = SerializationStrategy(
        { table, info -> table.integer(info.columnName) },
        { field, result, columnName, tableName -> result.columnValue<Int>(columnName, tableName) },
        { value -> value }
)

val longSerialization = SerializationStrategy(
        { table, info -> table.long(info.columnName) },
        { type, result, columnName, tableName -> result.columnValue<Long>(columnName, tableName) },
        { value -> value }
)

val doubleSerialization = SerializationStrategy(
        { table, info -> table.decimal(info.columnName, 5, 4) },
        { field, result, columnName, tableName -> result.columnValue<Double>(columnName, tableName) },
        { value -> value }
)

val booleanSerialization = SerializationStrategy(
        { table, info -> table.bool(info.columnName) },
        { field, result, columnName, tableName -> result.columnValue<Boolean>(columnName, tableName) },
        { value -> value }
)

val localDateSerialization = VarCharSerializationStrategy(
        LocalDate::class,
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
        LocalDateTime::class,
        LOCAL_DATE_TIME_LEN,
        { field, result, columnName, tableName ->
            result.columnValue<String>(columnName, tableName)?.let { LocalDateTime.parse(it, DATE_TIME_FORMAT) }
        },
        { value -> DATE_TIME_FORMAT.format(value as LocalDateTime) }
)

val emailSerialization = VarCharSerializationStrategy(
        Email::class,
        LONG_TEXT_LEN,
        { field, result, columnName, tableName -> result.columnValue<String>(columnName, tableName)?.let { Email(it) } },
        { value -> (value as Email).email }
)

object IdSerializationStrategy : SerializationStrategy {
    val idSerialization = VarCharSerializationStrategy(
            Id::class,
            ID_LEN,
            { field, result, columnName, tableName -> result.columnValue<String>(columnName, tableName)?.let { field.type.kotlin.primaryConstructor?.call(it) as Id? } },
            { value -> (value as Id).id }
    )

    override fun tryGetColumnDefinition(table: TableDefinition, info: PropertyInfo<*>) = when {
        info.returnType.isSubtypeOf(Id::class.starProjectedType) -> idSerialization.getColumnDefinition(table, info)
        else -> null
    }

    override fun tryReadColumnValue(field: Field, resultRow: ResultRow, columnName: String, tableName: String): Any? = when {
        field.type.kotlin.isSubclassOf(Id::class) -> idSerialization.readColumnValue(field, resultRow, columnName, tableName)
        else -> SerializationStrategy.Unhandled
    }

    override fun tryDecodeValue(value: Any): Any? = when (value) {
        is Id -> idSerialization.decodeValue(value)
        else -> SerializationStrategy.Unhandled
    }
}

val defaultSerializationStrategies: SerializationStrategy = TypedSerializationStrategies()
        .with(intSerialization)
        .with(longSerialization)
        .with(stringSerialization)
        .with(dateSerializationAsLong)
        .with(doubleSerialization)
        .with(booleanSerialization)
        .with(localDateSerialization)
        .with(localDateTimeSerialization)
        .with(emailSerialization)
        .with(IdSerializationStrategy)

