package kuick.reflection

import java.lang.reflect.InvocationTargetException
import java.lang.reflect.Method
import java.lang.reflect.Modifier

class ReflectionInvocationException(val method: Method, val context: Collection<Any?>, parent: Throwable)
    : RuntimeException("Error calling method \n  - Method :  [$method\n  - Received context: [${context.joinToString("\n      + ")}]", parent)


@Throws(ReflectionInvocationException::class)
fun Any.invokeWithParams(handlerMethod: Method, params: Collection<Any?>): Any? {

    try {
        return handlerMethod.invoke(this, *params.toTypedArray())
    } catch (e: IllegalAccessException) {
        throw ReflectionInvocationException(handlerMethod, params, e)
    } catch (e: IllegalArgumentException) {
        throw ReflectionInvocationException(handlerMethod, params, e)
    } catch (e: InvocationTargetException) {
        throw ReflectionInvocationException(handlerMethod, params, e.targetException)
    }
}

fun Class<*>.nonStaticFields() = declaredFields.filterNot { Modifier.isStatic(it.modifiers) }

