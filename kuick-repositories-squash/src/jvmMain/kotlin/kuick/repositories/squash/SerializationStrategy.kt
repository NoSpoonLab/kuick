package kuick.repositories.squash

import kuick.repositories.annotations.*
import kuick.utils.fastForEach
import org.jetbrains.squash.definition.*
import kotlin.reflect.*
import kotlin.reflect.full.*
import kotlin.reflect.jvm.*

class PropertyInfo<T>(val prop: KProperty<T>) {
    //val primary = prop.javaField?.getAnnotation(Primary::class.java) != null
    val unique = prop.javaField?.getAnnotation(Unique::class.java) != null
    val withIndex = prop.javaField?.getAnnotation(Index::class.java) != null
    val maxLength = prop.javaField?.getAnnotation(MaxLength::class.java)?.maxLength
    val nullableProp = prop.returnType.isMarkedNullable
    val returnType = prop.returnType.classifier!!.starProjectedType
    val returnTypeClass = returnType.classifier as KClass<*>
    val columnName = prop.name.toSnakeCase()

    private fun String.toSnakeCase(): String = flatMap {
        if (it.isUpperCase()) listOf('_', it.toLowerCase()) else listOf(it)
    }.joinToString("")
}

//typealias GetTypedValueFunc = (KType) -> Any?
//inline operator fun <reified T> GetTypedValueFunc.invoke(): T? = this(T::class.starProjectedType) as T?

typealias GetTypedValueFunc = (KClass<*>) -> Any?
inline operator fun <reified T> GetTypedValueFunc.invoke(): T? = this(T::class) as T?
val KType.clazz get() = classifier as? KClass<*>?

interface SerializationStrategy {
    object Unhandled

    companion object {
        val UnhandledColumnDefinition: ColumnDefinition<*>? = null
    }

    fun tryGetColumnDefinition(table: TableDefinition, info: PropertyInfo<*>): ColumnDefinition<*>?

    /** Decodes a value obtained lazily from [getValue] that is called with a requested type to the [targetType] */
    fun tryDecodeValueLazy(targetType: KType, getValue: GetTypedValueFunc): Any?

    /** Encodes a typed value in a way that can be stored in the database */
    fun tryEncodeValue(value: Any): Any?
}

abstract class TypedSerializationStrategy<T : Any>(
        val clazz: KClass<T>
) : SerializationStrategy {
    val type = clazz.starProjectedType

    abstract fun getColumnDefinition(table: TableDefinition, info: PropertyInfo<*>): ColumnDefinition<Any?>
    abstract fun decodeValue(targetType: KType, getValue: GetTypedValueFunc): T?
    abstract fun encodeValue(value: Any): Any?

    final override fun tryGetColumnDefinition(table: TableDefinition, info: PropertyInfo<*>): ColumnDefinition<*>? {
        if (info.returnType == type) return getColumnDefinition(table, info)
        return SerializationStrategy.UnhandledColumnDefinition
    }

    final override fun tryDecodeValueLazy(targetType: KType, getValue: GetTypedValueFunc): Any? {
        if (targetType == type) return decodeValue(targetType, getValue)
        return SerializationStrategy.Unhandled
    }

    final override fun tryEncodeValue(value: Any): Any? {
        if (value::class.starProjectedType == type) return encodeValue(value)
        return SerializationStrategy.Unhandled
    }
}

inline fun <reified T : Any> SerializationStrategy(
        noinline getColumnDefinition: TableDefinition.(info: PropertyInfo<*>) -> ColumnDefinition<Any?>,
        noinline readColumnValue: KType.(getValue: GetTypedValueFunc) -> T?,
        noinline decodeValue: (value: Any) -> Any?
) = object : TypedSerializationStrategy<T>(T::class) {
    override fun getColumnDefinition(table: TableDefinition, info: PropertyInfo<*>): ColumnDefinition<Any?> =
            getColumnDefinition(table, info)

    override fun decodeValue(targetType: KType, getValue: GetTypedValueFunc): T? =
            readColumnValue(targetType, getValue)

    override fun encodeValue(value: Any): Any? = decodeValue(value)

}

class TypedSerializationStrategies(val strategies: Map<KClass<*>, TypedSerializationStrategy<out Any>> = mapOf()) : SerializationStrategy {
    override fun tryGetColumnDefinition(table: TableDefinition, info: PropertyInfo<*>): ColumnDefinition<*>? {
        val strategy = strategies[info.returnTypeClass] ?: return SerializationStrategy.UnhandledColumnDefinition
        return strategy.getColumnDefinition(table, info)
    }

    override fun tryDecodeValueLazy(targetType: KType, getValue: GetTypedValueFunc): Any? {
        val strategy = strategies[targetType.clazz] ?: return SerializationStrategy.Unhandled
        return strategy.decodeValue(targetType, getValue)
    }

    override fun tryEncodeValue(value: Any): Any? {
        val strategy = strategies[value::class] ?: return SerializationStrategy.Unhandled
        return strategy.encodeValue(value)
    }
}

class SerializationStrategies(val strategies: List<SerializationStrategy>) : SerializationStrategy {
    constructor(vararg strategies: SerializationStrategy) : this(strategies.toList())

    override fun tryGetColumnDefinition(table: TableDefinition, info: PropertyInfo<*>): ColumnDefinition<*>? {
        strategies.fastForEach { strategy ->
            val result = strategy.tryGetColumnDefinition(table, info)
            if (result != SerializationStrategy.UnhandledColumnDefinition) return result
        }
        return SerializationStrategy.UnhandledColumnDefinition
    }

    override fun tryDecodeValueLazy(targetType: KType, getValue: GetTypedValueFunc): Any? {
        strategies.fastForEach { strategy ->
            val result = strategy.tryDecodeValueLazy(targetType, getValue)
            if (result != SerializationStrategy.Unhandled) return result
        }
        return SerializationStrategy.Unhandled
    }

    override fun tryEncodeValue(value: Any): Any? {
        strategies.fastForEach { strategy ->
            val result = strategy.tryEncodeValue(value)
            if (result != SerializationStrategy.Unhandled) return result
        }
        return SerializationStrategy.Unhandled
    }
}

// Generates a new SerializationStrategy, trying to optimize the generated SerializationStrategy graph.
fun SerializationStrategy.with(next: SerializationStrategy): SerializationStrategy {
    if (next is TypedSerializationStrategy<*>) {
        when (this) {
            is TypedSerializationStrategy<*> -> return TypedSerializationStrategies().with(this).with(next)
            is TypedSerializationStrategies -> return TypedSerializationStrategies(strategies + mapOf(next.type.clazz!! to next))
            is SerializationStrategies -> {
                val last = strategies.lastOrNull()
                if (last is TypedSerializationStrategies || last is TypedSerializationStrategy<*>) {
                    return SerializationStrategies(this.strategies.dropLast(1) + last.with(next))
                }
            }
        }
    }

    return if (this is SerializationStrategies) SerializationStrategies(strategies + next) else SerializationStrategies(this, next)
}

operator fun SerializationStrategy.plus(next: SerializationStrategy): SerializationStrategy = with(next)

@Deprecated("", ReplaceWith("with(clazz, serialization)"))
fun <T : Any> SerializationStrategy.withSerialization(clazz: KClass<T>, serialization: TypedSerializationStrategy<T>): SerializationStrategy = with(serialization)
