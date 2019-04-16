package kuick.repositories.squash

import kuick.core.*
import kuick.db.domainTransaction
import kuick.json.Json
import kuick.models.Id
import kuick.repositories.*
import kuick.repositories.squash.orm.*
import kuick.utils.nonStaticFields
import org.jetbrains.squash.definition.*
import org.jetbrains.squash.expressions.*
import org.jetbrains.squash.schema.create
import kotlin.reflect.KClass
import kotlin.reflect.KProperty1
import kotlin.reflect.KType
import kotlin.reflect.full.*


@UseExperimental(KuickInternalWarning::class)
open class ModelRepositorySquash<I : Any, T : Any>(
        val modelClass: KClass<T>,
        override val idField: KProperty1<T, I>,
        baseSerializationStrategies: SerializationStrategy = defaultSerializationStrategies,
        fallbackStrategy: SerializationStrategy = JsonSerializationStrategy
) : ModelRepository<I, T> {

    @Deprecated("Use the main constructor instead")
    constructor(
            modelClass: KClass<T>,
            idField: KProperty1<T, I>,
            defaultMaxLength: Int = LONG_TEXT_LEN,
            serializationStrategies : Map<KType, TypedSerializationStrategy<out Any>>,
            fallbackStrategy: SerializationStrategy = JsonSerializationStrategy
    ) : this(modelClass, idField, TypedSerializationStrategies(serializationStrategies.map { it.key.clazz!! to it.value }.toMap()), fallbackStrategy)

    val serializationStrategies = baseSerializationStrategies + fallbackStrategy
    val properties = modelClass.java.nonStaticFields().map { field ->
        val prop = modelClass.declaredMemberProperties.firstOrNull { it.name == field.name }
                ?: throw IllegalStateException("Property not found for field: ${field.name}")
        //println("Registering field ${prop} with return type: ${prop.returnType}")
        PropertyInfo(prop)
    }
    val idProperty = properties.first { it.prop == idField }
    val table = ORMTableDefinition(modelClass, serializationStrategies).also { table ->
        for (info in properties) {
            val prop = info.prop
            val columnDefinition = serializationStrategies.tryGetColumnDefinition(table, info) ?: error("Can't find columnDefinition")
            val columnDefinitionWithNulability = if (info.nullableProp) columnDefinition.nullable() else columnDefinition
            table.put(prop as KProperty1<T, Any?>, columnDefinitionWithNulability)
        }
    }

    override suspend fun init() {
        domainTransaction { tr ->
            val squashTr = tr.squashTr()
            squashTr.databaseSchema().create(table)
            val tableNameQuoted = squashTr.connection.dialect.idSQL(table.compoundName)
            //squashTr.executeStatement("ALTER TABLE tableNameQuoted ADD PRIMARY KEY (${idProperty.columnName});")
            for (info in properties) {
                if (info.unique) {
                    squashTr.executeStatement("CREATE UNIQUE INDEX unique_${info.columnName} ON $tableNameQuoted (${info.columnName});")
                }
            }
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

    override suspend fun findBy(q: ModelQuery<T>): List<T> =
            domainTransaction { tr -> table.select(tr, q.tryGetAttributed()) { q.toSquash() } }

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
        is DecoratedModelQuery<T> -> this.base.toSquash() // Ignore
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

