package kuick.repositories.squash

import kuick.repositories.annotations.*
import kuick.util.*
import org.jetbrains.squash.definition.*
import org.jetbrains.squash.results.*
import java.lang.reflect.*
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

interface SerializationStrategy {
    object Unhandled

    companion object {
        val UnhandledColumnDefinition: ColumnDefinition<*>? = null
    }

    fun tryGetColumnDefinition(table: TableDefinition, info: PropertyInfo<*>): ColumnDefinition<*>?
    fun tryReadColumnValue(field: Field, resultRow: ResultRow, columnName: String, tableName: String): Any?
    fun tryDecodeValue(value: Any): Any?
}

abstract class TypedSerializationStrategy<T : Any>(
        val clazz: KClass<T>
) : SerializationStrategy {
    val type = clazz.starProjectedType

    abstract fun getColumnDefinition(table: TableDefinition, info: PropertyInfo<*>): ColumnDefinition<Any?>
    abstract fun readColumnValue(field: Field, resultRow: ResultRow, columnName: String, tableName: String): T?
    abstract fun decodeValue(value: Any): Any?

    final override fun tryGetColumnDefinition(table: TableDefinition, info: PropertyInfo<*>): ColumnDefinition<*>? {
        if (info.returnType == type) return getColumnDefinition(table, info)
        return SerializationStrategy.UnhandledColumnDefinition
    }

    final override fun tryReadColumnValue(field: Field, resultRow: ResultRow, columnName: String, tableName: String): Any? {
        if (field.type.kotlin == type) return readColumnValue(field, resultRow, columnName, tableName)
        return SerializationStrategy.Unhandled
    }

    final override fun tryDecodeValue(value: Any): Any? {
        if (value::class.starProjectedType == type) return decodeValue(value)
        return SerializationStrategy.Unhandled
    }
}

inline fun <reified T : Any> SerializationStrategy(
        noinline getColumnDefinition: (table: TableDefinition, info: PropertyInfo<*>) -> ColumnDefinition<Any?>,
        noinline readColumnValue: (field: Field, resultRow: ResultRow, columnName: String, tableName: String) -> T?,
        noinline decodeValue: (value: Any) -> Any?
) = object : TypedSerializationStrategy<T>(T::class) {
    override fun getColumnDefinition(table: TableDefinition, info: PropertyInfo<*>): ColumnDefinition<Any?> =
            getColumnDefinition(table, info)

    override fun readColumnValue(field: Field, resultRow: ResultRow, columnName: String, tableName: String): T? =
            readColumnValue(field, resultRow, columnName, tableName)

    override fun decodeValue(value: Any): Any? = decodeValue(value)

}

class TypedSerializationStrategies(val strategies: Map<KType, TypedSerializationStrategy<out Any>> = mapOf()) : SerializationStrategy {
    override fun tryGetColumnDefinition(table: TableDefinition, info: PropertyInfo<*>): ColumnDefinition<*>? {
        val strategy = strategies[info.returnType] ?: return SerializationStrategy.UnhandledColumnDefinition
        return strategy.getColumnDefinition(table, info)
    }

    override fun tryReadColumnValue(field: Field, resultRow: ResultRow, columnName: String, tableName: String): Any? {
        val strategy = strategies[field.type.kotlin.starProjectedType] ?: return SerializationStrategy.Unhandled
        return strategy.readColumnValue(field, resultRow, columnName, tableName)
    }

    override fun tryDecodeValue(value: Any): Any? {
        val strategy = strategies[value::class.starProjectedType] ?: return SerializationStrategy.Unhandled
        return strategy.decodeValue(value)
    }
}

class ComposableStrategies(val strategies: List<SerializationStrategy>) : SerializationStrategy {
    constructor(vararg strategies: SerializationStrategy) : this(strategies.toList())

    override fun tryGetColumnDefinition(table: TableDefinition, info: PropertyInfo<*>): ColumnDefinition<*>? {
        strategies.fastForEach { strategy ->
            val result = strategy.tryGetColumnDefinition(table, info)
            if (result != SerializationStrategy.UnhandledColumnDefinition) return result
        }
        return SerializationStrategy.UnhandledColumnDefinition
    }

    override fun tryReadColumnValue(field: Field, resultRow: ResultRow, columnName: String, tableName: String): Any? {
        strategies.fastForEach { strategy ->
            val result = strategy.tryReadColumnValue(field, resultRow, columnName, tableName)
            if (result != SerializationStrategy.Unhandled) return result
        }
        return SerializationStrategy.Unhandled
    }

    override fun tryDecodeValue(value: Any): Any? {
        strategies.fastForEach { strategy ->
            val result = strategy.tryDecodeValue(value)
            if (result != SerializationStrategy.Unhandled) return result
        }
        return SerializationStrategy.Unhandled
    }
}

// Tries to reuse a TypedSerializationStrategies, in the order of the strategies would be preserved
// (either this is a TypedSerializationStrategies or the last element of a ComposableStrategies one is)
// If not, it would use a ComposableStrategies
fun SerializationStrategy.with(next: SerializationStrategy): SerializationStrategy {
    if (next is TypedSerializationStrategy<*>) {
        when {
            this is TypedSerializationStrategies -> {
                return TypedSerializationStrategies(strategies + mapOf(next.type to next))
            }
            this is ComposableStrategies && this.strategies.lastOrNull() is TypedSerializationStrategies -> {
                return ComposableStrategies(this.strategies.dropLast(1) + ((this.strategies.last() as TypedSerializationStrategies).with(next)))
            }
        }
    }

    return if (this is ComposableStrategies) ComposableStrategies(this.strategies + next) else ComposableStrategies(this, next)
}

@Deprecated("", ReplaceWith("with(clazz, serialization)"))
fun <T : Any> SerializationStrategy.withSerialization(clazz: KClass<T>, serialization: TypedSerializationStrategy<T>): SerializationStrategy = with(serialization)
