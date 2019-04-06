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
    object Unhandled

    companion object {
        val UnhandledColumnDefinition: ColumnDefinition<*>? = null
    }

    fun tryGetColumnDefinition(table: TableDefinition, info: PropertyInfo<*>): ColumnDefinition<*>?
    fun tryReadColumnValue(field: Field, resultRow: ResultRow, columnName: String, tableName: String): Any?
    fun tryDecodeValue(value: Any): Any?
}

open class SerializationStrategy<T : Any>(
        val clazz: KClass<T>,
        val getColumnDefinition: (table: TableDefinition, info: PropertyInfo<*>) -> ColumnDefinition<Any?>,
        val readColumnValue: (field: Field, resultRow: ResultRow, columnName: String, tableName: String) -> T?,
        val decodeValue: (value: Any) -> Any?
) : BaseSerializationStrategy {
    val type = clazz.starProjectedType

    final override fun tryGetColumnDefinition(table: TableDefinition, info: PropertyInfo<*>): ColumnDefinition<*>? {
        if (info.returnType == type) return getColumnDefinition(table, info)
        return BaseSerializationStrategy.UnhandledColumnDefinition
    }

    final override fun tryReadColumnValue(field: Field, resultRow: ResultRow, columnName: String, tableName: String): Any? {
        if (field.type.kotlin == type) return readColumnValue(field, resultRow, columnName, tableName)
        return BaseSerializationStrategy.Unhandled
    }

    final override fun tryDecodeValue(value: Any): Any? {
        if (value::class.starProjectedType == type) return decodeValue(value)
        return BaseSerializationStrategy.Unhandled
    }
}

class VarCharSerializationStrategy<T : Any>(
        clazz: KClass<T>,
        varcharLength: Int,
        readColumnValue: (Field, ResultRow, String, String) -> T?,
        decodeValue: (Any) -> Any?
) : SerializationStrategy<T>(clazz, { table, info -> table.varchar(info.columnName, info.maxLength ?: varcharLength) }, readColumnValue, decodeValue) {
    fun withLength(length: Int) = VarCharSerializationStrategy(clazz, length, readColumnValue, decodeValue)
}

val dateSerializationAsDateTime = SerializationStrategy<Date>(
        Date::class,
        { table, info -> table.datetime(info.columnName) },
        { type, result, columnName, tableName ->
            result.columnValue<LocalDateTime>(columnName, tableName)?.let {
                val zoneOffset = ZoneOffset.UTC.normalized().rules.getOffset(it)
                Date.from(it.toInstant(zoneOffset))
            }
        },
        { value -> LocalDateTime.ofInstant((value as Date).toInstant(), ZoneOffset.UTC.normalized()) }
)

val dateSerializationAsLong = SerializationStrategy<Date>(
        Date::class,
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
        Int::class,
        { table, info -> table.integer(info.columnName) },
        { field, result, columnName, tableName -> result.columnValue<Int>(columnName, tableName) },
        { value -> value }
)

val longSerialization = SerializationStrategy(
        Long::class,
        { table, info -> table.long(info.columnName) },
        { type, result, columnName, tableName -> result.columnValue<Long>(columnName, tableName) },
        { value -> value }
)

val doubleSerialization = SerializationStrategy(
        Double::class,
        { table, info -> table.decimal(info.columnName, 5, 4) },
        { field, result, columnName, tableName -> result.columnValue<Double>(columnName, tableName) },
        { value -> value }
)

val booleanSerialization = SerializationStrategy(
        Boolean::class,
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

class SerializationStrategies(val strategies: Map<KType, SerializationStrategy<out Any>> = mapOf()) : BaseSerializationStrategy {
    override fun tryGetColumnDefinition(table: TableDefinition, info: PropertyInfo<*>): ColumnDefinition<*>? {
        val strategy = strategies[info.returnType] ?: return BaseSerializationStrategy.UnhandledColumnDefinition
        return strategy.getColumnDefinition.invoke(table, info)
    }

    override fun tryReadColumnValue(field: Field, resultRow: ResultRow, columnName: String, tableName: String): Any? {
        val strategy = strategies[field.type.kotlin.starProjectedType] ?: return BaseSerializationStrategy.Unhandled
        return strategy.readColumnValue(field, resultRow, columnName, tableName)
    }

    override fun tryDecodeValue(value: Any): Any? {
        val strategy = strategies[value::class.starProjectedType] ?: return BaseSerializationStrategy.Unhandled
        return strategy.decodeValue(value)
    }
}

class ComposableStrategies(val first: BaseSerializationStrategy, val second: BaseSerializationStrategy) : BaseSerializationStrategy {
    override fun tryGetColumnDefinition(table: TableDefinition, info: PropertyInfo<*>): ColumnDefinition<*>? {
        return first.tryGetColumnDefinition(table, info) ?: second.tryGetColumnDefinition(table, info)
    }

    override fun tryReadColumnValue(field: Field, resultRow: ResultRow, columnName: String, tableName: String): Any? {
        val result = first.tryReadColumnValue(field, resultRow, columnName, tableName)
        if (result != BaseSerializationStrategy.Unhandled) return result
        return second.tryReadColumnValue(field, resultRow, columnName, tableName)
    }

    override fun tryDecodeValue(value: Any): Any? {
        val result = first.tryDecodeValue(value)
        if (result != BaseSerializationStrategy.Unhandled) return result
        return second.tryDecodeValue(value)
    }
}

fun <T : Any> BaseSerializationStrategy.with(serialization: SerializationStrategy<T>): BaseSerializationStrategy =
        when {
            this is SerializationStrategies -> SerializationStrategies(strategies + mapOf(serialization.clazz.starProjectedType to serialization))
            this is ComposableStrategies && this.second is SerializationStrategies -> ComposableStrategies(this.first, this.second.with(serialization))
            else -> ComposableStrategies(this, SerializationStrategies(mapOf(serialization.clazz.starProjectedType to serialization)))
        }

fun BaseSerializationStrategy.with(next: BaseSerializationStrategy) =
        ComposableStrategies(this, next)

@Deprecated("", ReplaceWith("with(clazz, serialization)"))
fun <T : Any> BaseSerializationStrategy.withSerialization(clazz: KClass<T>, serialization: SerializationStrategy<T>): BaseSerializationStrategy = with(serialization)

inline fun <reified T> type(): KType = T::class.starProjectedType

object IdSerializationStrategy : BaseSerializationStrategy {
    val idSerialization = VarCharSerializationStrategy(
            Id::class,
            ID_LEN,
            { field, result, columnName, tableName -> result.columnValue<String>(columnName, tableName)?.let { field.type.kotlin.primaryConstructor?.call(it) as Id? } },
            { value -> (value as Id).id }
    )

    override fun tryGetColumnDefinition(table: TableDefinition, info: PropertyInfo<*>) = when {
        info.returnType.isSubtypeOf(type<Id>()) -> idSerialization.getColumnDefinition(table, info)
        else -> null
    }

    override fun tryReadColumnValue(field: Field, resultRow: ResultRow, columnName: String, tableName: String): Any? = when {
        field.type.kotlin.isSubclassOf(Id::class) -> idSerialization.readColumnValue.invoke(field, resultRow, columnName, tableName)
        else -> BaseSerializationStrategy.Unhandled
    }

    override fun tryDecodeValue(value: Any): Any? = when (value) {
        is Id -> idSerialization.decodeValue.invoke(value)
        else -> BaseSerializationStrategy.Unhandled
    }
}

val defaultSerializationStrategies: BaseSerializationStrategy = SerializationStrategies()
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

