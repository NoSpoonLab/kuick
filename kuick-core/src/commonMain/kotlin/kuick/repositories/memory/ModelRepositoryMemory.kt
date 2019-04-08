package kuick.repositories.memory


import kuick.repositories.*
import kotlin.reflect.KClass
import kotlin.reflect.KProperty1


open class ModelRepositoryMemory<I : Any, T : Any>(
        val modelClass: KClass<T>,
        override val idField: KProperty1<T, I>
) : ModelRepository<I, T>, ScoredViewRepository<I, T> {

    val table = mutableMapOf<I, T>()

    override suspend fun init() {}

    override suspend fun insert(t: T): T = update(t)

    override suspend fun update(t: T): T {
        table.put(id(t), t)
        return t
    }

    override suspend fun updateBy(t: T, q: ModelQuery<T>): T {
        findBy(q).forEach { update(t) }
        return t
    }

    override suspend fun delete(i: I) {
        table.remove(i)
    }

    override suspend fun deleteBy(q: ModelQuery<T>) =
            findBy(q).forEach { delete(idField.get(it)) }

    override suspend fun findBy(q: ModelQuery<T>): List<T> =
            table.values.filter { it.match(q) }

    override suspend fun getAll(): List<T> = table.values.toList()

    override suspend fun searchBy(q: ModelQuery<T>): List<ScoredModel<T>> =
            findBy(q)
                    .map { ScoredModel(1.0F, it) }

    private fun id(t: T): I = idField.get(t)

    private fun T.match(q: ModelQuery<T>): Boolean = when (q) {
        is FieldUnop<T, *> -> {
            when (q) {
                is FieldIsNull<T, *> -> q.field.get(this) == null
                else -> throw NotImplementedError("Missing implementation of .toSquash() for ${this}")
            }
        }
        is FieldBinop<T, *, *> -> {
            when (q) {
                is FieldEqs<T, *> ->
                    q.field.get(this) == q.value
                is FieldLike<T> ->
                    q.value?.let { q.field.get(this)?.contains(q.value) ?: false } ?: false
                is FieldGt<T, *> ->
                    compare(q).let { if (it == null) false else it > 0 }
                is FieldGte<T, *> ->
                    compare(q).let { if (it == null) false else it >= 0 }
                is FieldLt<T, *> ->
                    compare(q).let { if (it == null) false else it < 0 }
                is FieldLte<T, *> ->
                    compare(q).let { if (it == null) false else it <= 0 }
                is FieldWithin<T, *> ->
                    q.value?.contains(q.field(this)) ?: false
                is FieldWithinComplex<T, *> ->
                    q.value?.contains(q.field(this)) ?: false
                else -> throw NotImplementedError("Missing implementation of .toSquash() for ${this}")
            }
        }
        is FilterExpAnd<T> -> this.match(q.left) and this.match(q.right)
        is FilterExpOr<T> -> this.match(q.left) or this.match(q.right)
        is DecoratedModelQuery<T> -> this.match(q.base)

        else -> throw NotImplementedError("Missing implementation of .toSquash() for ${this}")
    }

    private fun T.compare(q: SimpleFieldBinop<T, *>) =
            (q.field.get(this) as Comparable<Any>?)?.compareTo(q.value as Comparable<Any>)

}

