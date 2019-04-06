package kuick.repositories.squash

import kuick.repositories.annotations.*
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

class ComposableStrategies(val first: SerializationStrategy, val second: SerializationStrategy) : SerializationStrategy {
    override fun tryGetColumnDefinition(table: TableDefinition, info: PropertyInfo<*>): ColumnDefinition<*>? {
        val result = first.tryGetColumnDefinition(table, info)
        if (result != SerializationStrategy.UnhandledColumnDefinition) return result
        return second.tryGetColumnDefinition(table, info)
    }

    override fun tryReadColumnValue(field: Field, resultRow: ResultRow, columnName: String, tableName: String): Any? {
        val result = first.tryReadColumnValue(field, resultRow, columnName, tableName)
        if (result != SerializationStrategy.Unhandled) return result
        return second.tryReadColumnValue(field, resultRow, columnName, tableName)
    }

    override fun tryDecodeValue(value: Any): Any? {
        val result = first.tryDecodeValue(value)
        if (result != SerializationStrategy.Unhandled) return result
        return second.tryDecodeValue(value)
    }
}

// Tries to reuse a TypedSerializationStrategies, in the order of the strategies would be preserved
// (either this is a TypedSerializationStrategies or the last element of a ComposableStrategies one is)
// If not, it would use a ComposableStrategies
fun <T : Any> SerializationStrategy.with(serialization: TypedSerializationStrategy<T>): SerializationStrategy =
        when {
            this is TypedSerializationStrategies -> TypedSerializationStrategies(strategies + mapOf(serialization.clazz.starProjectedType to serialization))
            this is ComposableStrategies && this.second is TypedSerializationStrategies -> ComposableStrategies(this.first, this.second.with(serialization))
            else -> ComposableStrategies(this, TypedSerializationStrategies(mapOf(serialization.clazz.starProjectedType to serialization)))
        }

fun SerializationStrategy.with(next: SerializationStrategy) = ComposableStrategies(this, next)

@Deprecated("", ReplaceWith("with(clazz, serialization)"))
fun <T : Any> SerializationStrategy.withSerialization(clazz: KClass<T>, serialization: TypedSerializationStrategy<T>): SerializationStrategy = with(serialization)
