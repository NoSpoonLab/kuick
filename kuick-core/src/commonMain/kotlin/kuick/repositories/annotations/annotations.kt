package kuick.repositories.annotations

@Retention(AnnotationRetention.RUNTIME)
@Target(AnnotationTarget.FIELD)
annotation class MaxLength(val maxLength: Int)

//@Retention(AnnotationRetention.RUNTIME)
//@Target(AnnotationTarget.FIELD)
//annotation class Primary

@Retention(AnnotationRetention.RUNTIME)
@Target(AnnotationTarget.FIELD)
annotation class Unique

@Retention(AnnotationRetention.RUNTIME)
@Target(AnnotationTarget.CLASS, AnnotationTarget.PROPERTY)
annotation class DbName(val name: String)
