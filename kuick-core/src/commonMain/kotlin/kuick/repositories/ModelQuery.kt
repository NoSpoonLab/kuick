package kuick.repositories

import kotlin.internal.Exact
import kotlin.reflect.KProperty1


sealed class ModelQuery<T : Any>

sealed class ModelFilterExp<T : Any> : ModelQuery<T>()

// NOT
data class FilterExpNot<T : Any>(val exp: ModelFilterExp<T>) : ModelFilterExp<T>()

//fun <T : Any> ModelFilterExp<T>.not() = FilterExpNot(this)
fun <T : Any> not(exp: ModelFilterExp<T>) = FilterExpNot(exp)

// AND
data class FilterExpAnd<T : Any>(val left: ModelFilterExp<T>, val right: ModelFilterExp<T>) : ModelFilterExp<T>()

infix fun <T : Any> ModelFilterExp<T>.and(right: ModelFilterExp<T>) = FilterExpAnd(this, right)

// OR
data class FilterExpOr<T : Any>(val left: ModelFilterExp<T>, val right: ModelFilterExp<T>) : ModelFilterExp<T>()

infix fun <T : Any> ModelFilterExp<T>.or(right: ModelFilterExp<T>) = FilterExpOr(this, right)


// Field Unary Operator
abstract class FieldUnop<T : Any, F : Any>(val field: KProperty1<T, F?>) : ModelFilterExp<T>()

// IS_NULL
class FieldIsNull<T : Any, F : Any>(field: KProperty1<T, F?>) : FieldUnop<T, F>(field)

fun <T : Any, F : Any> KProperty1<T, F?>.isNull() = FieldIsNull(this)


// Field Binary Operator
abstract class FieldBinop<T : Any, F : Any, V : Any>(val field: KProperty1<T, F?>, val value: V) : ModelFilterExp<T>()

// Field Binary Operator for simple queries
abstract class SimpleFieldBinop<T : Any, V : Any>(field: KProperty1<T, V?>, value: V) : FieldBinop<T, V, V>(field, value)

// EQ ==
class FieldEqs<T : Any, V : Any>(field: KProperty1<T, V?>, value: V) : SimpleFieldBinop<T, V>(field, value)

infix fun <T : Any, V : Any> KProperty1<T, @Exact V?>.eq(value: V) = FieldEqs(this, value)

// LIKE ~=
class FieldLike<T : Any>(field: KProperty1<T, String?>, value: String) : SimpleFieldBinop<T, String>(field, value)

infix fun <T : Any> KProperty1<T, String?>.like(value: String) = FieldLike(this, value)

// GT ==
class FieldGt<T : Any, V : Comparable<V>>(field: KProperty1<T, V?>, value: V) : SimpleFieldBinop<T, V>(field, value)

infix fun <T : Any, V : Comparable<V>> KProperty1<T, @Exact V?>.gt(value: V) = FieldGt(this, value)

// GTE ==
class FieldGte<T : Any, V : Comparable<V>>(field: KProperty1<T, V?>, value: V) : SimpleFieldBinop<T, V>(field, value)

infix fun <T : Any, V : Comparable<V>> KProperty1<T, @Exact V?>.gte(value: V) = FieldGte(this, value)

// LT ==
class FieldLt<T : Any, V : Comparable<V>>(field: KProperty1<T, V?>, value: V) : SimpleFieldBinop<T, V>(field, value)

infix fun <T : Any, V : Comparable<V>> KProperty1<T, @Exact V?>.lt(value: V) = FieldLt(this, value)

// LTE ==
class FieldLte<T : Any, V : Comparable<V>>(field: KProperty1<T, V?>, value: V) : SimpleFieldBinop<T, V>(field, value)

infix fun <T : Any, V : Comparable<V>> KProperty1<T, @Exact V?>.lte(value: V) = FieldLte(this, value)

// Field Binary Operator for collection queries
abstract class FieldBinopOnSet<T : Any, V : Any>(field: KProperty1<T, V?>, value: Set<V>) : FieldBinop<T, V, Set<V>>(field, value)

// within ==
// for primitive types
class FieldWithin<T : Any, V : Any>(field: KProperty1<T, V?>, value: Set<V>) : FieldBinopOnSet<T, V>(field, value)

// for complex types
class FieldWithinComplex<T : Any, V : Any>(field: KProperty1<T, V?>, value: Set<V>) : FieldBinopOnSet<T, V>(field, value)

inline infix fun <T : Any, reified V : Any> KProperty1<T, V?>.within(value: @Exact Set<V>) =
    if (isBasicType<V>())
        FieldWithin(this, value)
    else
        FieldWithinComplex(this, value)

inline fun <reified V : Any> isBasicType() = setOf(Boolean::class, Number::class, String::class, Char::class).any { it == V::class }

