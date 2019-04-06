package kuick.repositories.squash

import kuick.db.domainTransaction
import kuick.json.Json
import kuick.models.Id
import kuick.repositories.*
import kuick.repositories.annotations.*
import kuick.repositories.squash.orm.*
import kuick.utils.nonStaticFields
import org.jetbrains.squash.definition.*
import org.jetbrains.squash.expressions.*
import org.jetbrains.squash.schema.create
import kotlin.reflect.KClass
import kotlin.reflect.KProperty1
import kotlin.reflect.KType
import kotlin.reflect.full.*
import kotlin.reflect.jvm.*


open class ModelRepositorySquash<I : Any, T : Any>(
        val modelClass: KClass<T>,
        val idField: KProperty1<T, I>,
        val defaultMaxLength: Int = LONG_TEXT_LEN,
        serializationStrategies : Map<KType,SerializationStrategy<out Any>> = defaultSerializationStrategies.strategies
) : ModelRepository<I, T> {

    val table = ORMTableDefinition(serializationStrategies, modelClass)

    init {
        for (field in modelClass.java.nonStaticFields()) {
            val prop = modelClass.declaredMemberProperties.firstOrNull { it.name == field.name }
                    ?: throw IllegalStateException("Property not found for field: ${field.name}")

            val info = PropertyInfo(prop)
            //println("Registering field ${prop} with return type: ${prop.returnType}")

            with(table) {
                var columnDefinition: ColumnDefinition<Any?> = when {
                    serializationStrategies.containsKey(info.returnType) -> {
                        var strategy = serializationStrategies[info.returnType]!!
                        if (strategy is VarCharSerializationStrategy && info.maxLength!=null) strategy= strategy.withLength(info.maxLength)
                        strategy.getColumnDefinition.invoke(table,info.columnName)
                    }
                    info.returnType.isSubtypeOf(type<Id>()) -> serializationStrategies[type<Id>()]!!.getColumnDefinition.invoke(table,info.columnName)
                    else -> varchar(info.columnName, info.maxLength?:defaultMaxLength)
                }
                if (info.nullableProp) columnDefinition = columnDefinition.nullable()
                prop to columnDefinition
            }
        }
    }

    override suspend fun init() {
        domainTransaction { tr ->
            tr.squashTr().databaseSchema().create(table)
        }
    }

    override suspend fun insert(t: T): T = domainTransaction { tr -> table.insert(tr, t) }

    override suspend fun update(t: T): T = domainTransaction { tr ->
        table.update(tr, t) {
            (idField eq (idField.get(t))).toSquash()
        }
    }

    override suspend fun updateBy(t: T, q: ModelQuery<T>): T {
        domainTransaction { tr -> table.update(tr, t) { q.toSquash() } }
        return t
    }

    override suspend fun delete(i: I) = domainTransaction { tr ->
        table.delete(tr) {
            (idField eq i).toSquash()
        }
    }

    override suspend fun deleteBy(q: ModelQuery<T>) = domainTransaction { tr ->
        table.delete(tr) {
            q.toSquash()
        }
    }

    override suspend fun findById(i: I): T? = findOneBy(idField eq i)

    override suspend fun findOneBy(q: ModelQuery<T>): T? =
            domainTransaction { tr -> table.selectOne(tr) { q.toSquash() } }

    override suspend fun findBy(q: ModelQuery<T>): List<T> =
            domainTransaction { tr -> table.select(tr) { q.toSquash() } }

    override suspend fun getAll(): List<T> = domainTransaction { tr -> table.selectAll(tr) }

    private fun ModelQuery<T>.toSquash(): Expression<Boolean> = when (this) {
        is FieldEqs<T, *> -> when (value) {
            is Id -> table[field] eq (value as Id).id
            else -> table[field] eq value
        }
        is FieldGt<T, *> -> table[field] gt value
        is FieldGte<T, *> -> table[field] gteq value
        is FieldLt<T, *> -> table[field] lt value
        is FieldLte<T, *> -> table[field] lteq value
        is FieldWithin<T, *> -> table[field] within (value ?: emptySet())
        is FieldWithinComplex<T, *> -> {
            val _value = value ?: emptySet()
            when {
                _value.isNotEmpty() && _value.first() is Id -> table[field] within (_value.map { (it as Id).id })
                else -> table[field] within _value.map { Json.toJson(it) }
            }
        }
        is FilterExpAnd<T> -> left.toSquash() and right.toSquash()
        is FilterExpOr<T> -> left.toSquash() or right.toSquash()
        is FilterExpNot<T> -> not(exp.toSquash())
        else -> throw NotImplementedError("Missing implementation of .toSquash() for ${this}")
    }


//    fun <T> Collection<T>.isCollectionOfBasicType() = setOf(Boolean::class, Number::class, Char::class, String::class)
//        .contains(this::class::typeParameters[0])
//
//    fun KClass<*>.isBasicType() = setOf<KClass<*>>(Boolean::class, Number::class, Char::class, String::class)
//        .contains(this)
//
//    fun KClass<Collection<*>>.isCollectionOfBasicType() = this.typeParameters.get(0).
//        .contains(this)
}

