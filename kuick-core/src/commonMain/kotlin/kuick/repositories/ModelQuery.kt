package kuick.repositories

import kotlin.internal.Exact
import kotlin.reflect.KClass
import kotlin.reflect.KProperty1


sealed class ModelQuery<T : Any>

sealed class ModelFilterExp<T : Any> : ModelQuery<T>()

interface OrderByDescriptor<T> {
    val list: List<OrderBy<T>>
}
data class OrderBy<T>(val prop: KProperty1<T, *>, val ascending: Boolean) : OrderByDescriptor<T> {
    override val list = listOf(this)
}
data class OrderByMultiple<T>(val other: List<OrderByDescriptor<T>>) : OrderByDescriptor<T> {
    override val list = other.flatMap { it.list }
}

operator fun <T> OrderByDescriptor<T>.plus(other: OrderByDescriptor<T>) = OrderByMultiple(listOf(this, other))

fun <T> KProperty1<T, Any>.asc(): OrderByDescriptor<T> = OrderBy(this, ascending = true)
fun <T> KProperty1<T, Any>.desc(): OrderByDescriptor<T> = OrderBy(this, ascending = false)

open class DecoratedModelQuery<T : Any>(val base: ModelQuery<T>) : ModelQuery<T>()

class AttributedModelQuery<T : Any>(base: ModelQuery<T>, val skip: Long = 0L, val limit: Int? = null, val orderBy: OrderByDescriptor<T>? = null) : DecoratedModelQuery<T>(base)

// @TODO: Could we want to find in children? Should we need a vistor for this graph?
fun <T : Any> ModelQuery<T>.tryGetAttributed(): AttributedModelQuery<T>? = this as? AttributedModelQuery<T>

abstract class FilterExpBinopLogic<T: Any>(val op: String) : ModelFilterExp<T>() {
    abstract val left: ModelFilterExp<T>
    abstract val right: ModelFilterExp<T>
}

abstract class FilterExpUnopLogic<T: Any>(val op: String) : ModelFilterExp<T>() {
    abstract val exp: ModelFilterExp<T>
}


// NOT
data class FilterExpNot<T : Any>(override val exp: ModelFilterExp<T>) : FilterExpUnopLogic<T>("NOT")

//fun <T : Any> ModelFilterExp<T>.not() = FilterExpNot(this)
fun <T : Any> not(exp: ModelFilterExp<T>) = FilterExpNot(exp)

// AND
data class FilterExpAnd<T : Any>(override val left: ModelFilterExp<T>, override val right: ModelFilterExp<T>) : FilterExpBinopLogic<T>("AND")

infix fun <T : Any> ModelFilterExp<T>.and(right: ModelFilterExp<T>) = FilterExpAnd(this, right)

// OR
data class FilterExpOr<T : Any>(override val left: ModelFilterExp<T>, override val right: ModelFilterExp<T>) : FilterExpBinopLogic<T>("OR")

infix fun <T : Any> ModelFilterExp<T>.or(right: ModelFilterExp<T>) = FilterExpOr(this, right)


// Field Unary Operator
abstract class FieldUnop<T : Any, F : Any>(val field: KProperty1<T, F?>) : ModelFilterExp<T>()

// IS_NULL
class FieldIsNull<T : Any, F : Any>(field: KProperty1<T, F?>) : FieldUnop<T, F>(field)

fun <T : Any, F : Any> KProperty1<T, F?>.isNull() = FieldIsNull(this)


// Field Binary Operator
abstract class FieldBinop<T : Any, F : Any, V : Any>(val field: KProperty1<T, F?>, val value: V?) : ModelFilterExp<T>()

// Field Binary Operator for simple queries
abstract class SimpleFieldBinop<T : Any, V : Any>(field: KProperty1<T, V?>, value: V?, val op: String) : FieldBinop<T, V, V>(field, value)

// EQ =
class FieldEqs<T : Any, V : Any>(field: KProperty1<T, V?>, value: V?) : SimpleFieldBinop<T, V>(field, value, "=")

infix fun <T : Any, V : Any> KProperty1<T, @Exact V?>.eq(value: V?) = FieldEqs(this, value)

// NE <>
class FieldNeq<T : Any, V : Any>(field: KProperty1<T, V?>, value: V?) : SimpleFieldBinop<T, V>(field, value, "<>")

infix fun <T : Any, V : Any> KProperty1<T, @Exact V?>.ne(value: V?) = FieldNeq(this, value)

// LIKE ~=
class FieldLike<T : Any>(field: KProperty1<T, String?>, value: String) : SimpleFieldBinop<T, String>(field, value, "~=")

infix fun <T : Any> KProperty1<T, String?>.like(value: String) = FieldLike(this, value)

// GT >
class FieldGt<T : Any, V : Comparable<V>>(field: KProperty1<T, V?>, value: V) : SimpleFieldBinop<T, V>(field, value, ">")

infix fun <T : Any, V : Comparable<V>> KProperty1<T, @Exact V?>.gt(value: V) = FieldGt(this, value)

// GTE >=
class FieldGte<T : Any, V : Comparable<V>>(field: KProperty1<T, V?>, value: V) : SimpleFieldBinop<T, V>(field, value, ">=")

infix fun <T : Any, V : Comparable<V>> KProperty1<T, @Exact V?>.gte(value: V) = FieldGte(this, value)

// LT <
class FieldLt<T : Any, V : Comparable<V>>(field: KProperty1<T, V?>, value: V) : SimpleFieldBinop<T, V>(field, value, "<")

infix fun <T : Any, V : Comparable<V>> KProperty1<T, @Exact V?>.lt(value: V) = FieldLt(this, value)

// LTE <=
class FieldLte<T : Any, V : Comparable<V>>(field: KProperty1<T, V?>, value: V) : SimpleFieldBinop<T, V>(field, value, "<=")

infix fun <T : Any, V : Comparable<V>> KProperty1<T, @Exact V?>.lte(value: V) = FieldLte(this, value)

// Field Binary Operator for collection queries
abstract class FieldBinopOnSet<T : Any, V : Any>(field: KProperty1<T, V?>, value: Set<V>) : FieldBinop<T, V, Set<V>>(field, value)

// within ==
// for primitive types
class FieldWithin<T : Any, V : Any>(field: KProperty1<T, V?>, value: Set<V>) : FieldBinopOnSet<T, V>(field, value)

// for complex types
class FieldWithinComplex<T : Any, V : Any>(field: KProperty1<T, V?>, value: Set<V>) : FieldBinopOnSet<T, V>(field, value)

inline infix fun <T : Any, reified V : Any> KProperty1<T, V?>.within(value: @Exact Set<V>) =
        if (isBasicType(V::class))
            FieldWithin(this, value)
        else
            FieldWithinComplex(this, value)


expect fun <V : Any> isBasicType(clazz: KClass<V>) : Boolean