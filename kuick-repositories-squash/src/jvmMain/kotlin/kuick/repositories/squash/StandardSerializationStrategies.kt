package kuick.repositories.squash

import kuick.json.*
import kuick.models.*
import kuick.repositories.squash.orm.*
import org.jetbrains.squash.definition.*
import java.math.*
import java.time.*
import java.util.*
import kotlin.reflect.*
import kotlin.reflect.full.*

class VarCharSerializationStrategy<T : Any>(
        clazz: KClass<T>,
        val varcharLength: Int,
        private val decodeValue: KType.(getValue: GetTypedValueFunc) -> T?,
        private val encodeValue: (value: Any) -> Any?
) : TypedSerializationStrategy<T>(clazz) {

    override fun getColumnDefinition(table: TableDefinition, info: PropertyInfo<*>): ColumnDefinition<Any?> = table.varchar(info.columnName, info.maxLength
            ?: varcharLength)

    override fun decodeValue(targetType: KType, getValue: (KClass<*>) -> Any?): T? = decodeValue.invoke(targetType, getValue)
    override fun encodeValue(value: Any): Any? = encodeValue.invoke(value)
    fun withLength(length: Int) = VarCharSerializationStrategy(clazz, length, decodeValue, encodeValue)
}

val dateSerializationAsDateTime = SerializationStrategy(
        { datetime(it.columnName) },
        { it<LocalDateTime>()?.let { Date.from(it.toInstant(ZoneOffset.UTC.normalized().rules.getOffset(it))) } },
        { LocalDateTime.ofInstant((it as Date).toInstant(), ZoneOffset.UTC.normalized()) }
)

val dateSerializationAsLong = SerializationStrategy({ long(it.columnName) }, { it<Long>()?.let { Date(it) } }, { (it as Date).time })
val stringSerialization = VarCharSerializationStrategy(String::class, LONG_TEXT_LEN, { it<String>() }, { it })
val intSerialization = SerializationStrategy({ integer(it.columnName) }, { it<Int>() }, { it })
val longSerialization = SerializationStrategy({ long(it.columnName) }, { it<Long>() }, { it })
val doubleSerialization = SerializationStrategy({ decimal(it.columnName, 5, 4) }, { it<BigDecimal>()?.toDouble() }, { it })
val booleanSerialization = SerializationStrategy({ bool(it.columnName) }, { it<Boolean>() }, { value -> value })
val uuidSerialization = SerializationStrategy({ uuid(it.columnName) }, { it<UUID>() }, { value -> value })

val localDateSerialization = VarCharSerializationStrategy(
        LocalDate::class,
        LOCAL_DATE_TIME_LEN,
        {
            it<String>()?.takeIf { it != "0000-00-00" }?.let { dateAsStr ->
                ignoreErrors { LocalDate.parse(dateAsStr, DATE_FORMAT) }
                        ?: ignoreErrors { LocalDate.parse(Json.fromJson<LocalDate>(dateAsStr).toString(), DATE_FORMAT) }
                        ?: error("Unknown date format [$dateAsStr]")
            }
        },
        { DATE_FORMAT.format(it as LocalDate) }
)

val localDateTimeSerialization = VarCharSerializationStrategy(LocalDateTime::class, LOCAL_DATE_TIME_LEN, { it<String>()?.let { LocalDateTime.parse(it, DATE_TIME_FORMAT) } }, { DATE_TIME_FORMAT.format(it as LocalDateTime) })
val emailSerialization = VarCharSerializationStrategy(Email::class, LONG_TEXT_LEN, { it<String>()?.let { Email(it) } }, { (it as Email).email })

object IdSerializationStrategy : SerializationStrategy {
    val idSerialization = VarCharSerializationStrategy(
            Id::class,
            ID_LEN,
            { val type = this; it<String>()?.let { type.clazz?.primaryConstructor?.call(it) as? Id? } },
            { (it as Id).id }
    )

    override fun tryGetColumnDefinition(table: TableDefinition, info: PropertyInfo<*>) = when {
        info.returnType.isSubtypeOf(Id::class.starProjectedType) -> idSerialization.getColumnDefinition(table, info)
        else -> null
    }

    override fun tryDecodeValueLazy(targetType: KType, getValue: GetTypedValueFunc): Any? = when {
        targetType.clazz!!.isSubclassOf(Id::class) -> idSerialization.decodeValue(targetType, getValue)
        else -> SerializationStrategy.Unhandled
    }

    override fun tryEncodeValue(value: Any): Any? = when (value) {
        is Id -> idSerialization.encodeValue(value)
        else -> SerializationStrategy.Unhandled
    }
}

object JsonSerializationStrategy : SerializationStrategy {
    override fun tryGetColumnDefinition(table: TableDefinition, info: PropertyInfo<*>): ColumnDefinition<*>? = table.varchar(info.columnName, info.maxLength ?: LONG_TEXT_LEN)
    override fun tryDecodeValueLazy(targetType: KType, getValue: GetTypedValueFunc): Any? = getValue<String>()?.let { Json.fromJson(it, targetType) }
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
        uuidSerialization,
        IdSerializationStrategy
)

val defaultFallbackSerializationStrategy = JsonSerializationStrategy
