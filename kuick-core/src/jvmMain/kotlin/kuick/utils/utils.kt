package kuick.utils

import java.lang.reflect.*

fun Class<*>.nonStaticFields() = declaredFields.filterNot { Modifier.isStatic(it.modifiers) }
