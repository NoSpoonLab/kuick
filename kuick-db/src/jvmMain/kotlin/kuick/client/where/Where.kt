package kuick.client.where

import kuick.orm.*
import kuick.repositories.*
import kotlin.internal.*
import kotlin.reflect.*
import kotlin.reflect.full.*

data class Where<T : Any>(
    private val builder: ModelWhereBuilder<T>,
    private val repo: ViewRepository<out Any, T>,
    private val query: ModelQuery<T>? = null,
    private val skip: Long = 0L,
    private val limit: Int? = null,
    private val orderBy: List<OrderBy<T>>? = null
) {
    fun skip(count: Long = 0L) = this.copy(skip = count)
    fun limit(count: Int?) = this.copy(limit = count)

    fun setWhere(block: ModelWhereBuilder<T>.(it: T) -> ModelQuery<T>): Where<T> {
        return this.copy(query = block(builder, builder.instance))
    }
    fun where(block: ModelWhereBuilder<T>.(it: T) -> ModelQuery<T>): Where<T> {
        val result = block(builder, builder.instance)
        return when (this.query) {
            null -> this.copy(query = result)
            else -> this.copy(query = FilterExpAnd(this.query as ModelFilterExp<T>, result as ModelFilterExp<T>))
        }
    }

    fun sortDesc(block: (it: T) -> KProperty0<*>): Where<T> =
        copy(orderBy = (orderBy ?: listOf()) + OrderBy(builder.propsByName[block(builder.instance).name]!!, false))

    fun sortAsc(block: (it: T) -> KProperty0<*>): Where<T> =
        copy(orderBy = (orderBy ?: listOf()) + OrderBy(builder.propsByName[block(builder.instance).name]!!, true))

    suspend fun count(skip: Long? = null, limit: Int? = null) = find(skip, limit).size

    suspend fun find(skip: Long? = null, limit: Int? = null): List<T> {
        return when (query) {
            null -> repo.getAll()
            else -> {
                // @TODO: We should support ALWAYS or null here (1=1)
                repo.findBy(AttributedModelQuery(query!!, skip ?: this.skip, this.limit ?: limit, orderBy?.let { OrderByMultiple(it) }))
            }
        }
    }
}

open class ModelWhereBuilder<T : Any>(
    val table: TableDefinition<T>
) {
    val instance by lazy { table.buildDummy() }
    val propsByName by lazy { table.clazz.memberProperties.associateBy { it.name } }

    private val <R : Any> KProperty0<@Exact R>.prop1 get() = (propsByName[this.name] as KProperty1<T, R?>?) ?: error("Can't find prop $name")

    infix fun <R : Any> KProperty0<@Exact R>.within(value: Set<R>) = FieldWithin(prop1, value)
    infix fun <R : Any> KProperty0<@Exact R>.eq(value: R?) = FieldEqs(prop1, value)
    infix fun <R : Any> KProperty0<@Exact R>.ne(value: R?) = FieldNeq(prop1, value)
    infix fun <R : Comparable<R>> KProperty0<@Exact R>.gt(value: R) = FieldGt(prop1, value)
    infix fun <R : Comparable<R>> KProperty0<@Exact R>.ge(value: R) = FieldGte(prop1, value)
    infix fun <R : Comparable<R>> KProperty0<@Exact R>.lt(value: R) = FieldGt(prop1, value)
    infix fun <R : Comparable<R>> KProperty0<@Exact R>.le(value: R) = FieldGte(prop1, value)
    infix fun KProperty0<String>.like(value: String) = FieldLike(prop1, value)

    infix fun ModelFilterExp<T>.and(that: ModelFilterExp<T>) = FilterExpAnd(this, that)
    infix fun ModelFilterExp<T>.or(that: ModelFilterExp<T>) = FilterExpOr(this, that)
    fun ModelFilterExp<T>.not() = FilterExpNot(this)
}

fun <T : Any> ViewRepository<out Any, T>.where(table: TableDefinition<T>): Where<T> = Where(ModelWhereBuilder(table), this)
fun <T : Any> ViewRepository<out Any, T>.where(table: TableDefinition<T>, block: ModelWhereBuilder<T>.(it: T) -> ModelQuery<T>): Where<T> = where(table).where(block)
inline fun <reified T : Any> ViewRepository<out Any, T>.where(noinline block: ModelWhereBuilder<T>.(it: T) -> ModelQuery<T>): Where<T> = where(TableDefinition(T::class), block)
inline val <reified T : Any> ViewRepository<out Any, T>.where: Where<T> get() = where(TableDefinition(T::class))
