package kuick.orm

import kuick.repositories.annotations.*
import kotlin.reflect.*
import kotlin.reflect.full.*

class ColumnDefinition<T : Any>(val table: TableDefinition<T>, val prop: KProperty1<T, *>) {
    val name = prop.findAnnotation<DbName>()?.name ?: prop.name
    val unique = prop.findAnnotation<Unique>() != null
    val maxLength = prop.findAnnotation<MaxLength>()?.maxLength
    val type = prop.returnType
    val clazz = type.classifier as KClass<*>?
    val nullable = type.isMarkedNullable
}

class TableDefinition<T : Any>(val clazz: KClass<T>) {
    val name = clazz.findAnnotation<DbName>()?.name ?: clazz.simpleName
    ?: error("Can't determine table name for $clazz")
    val columns = clazz.memberProperties.map { ColumnDefinition(this, it) }
}
