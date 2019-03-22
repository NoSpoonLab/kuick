package kuick.utils

import java.util.*
import kotlin.reflect.*

class WeakProperty<T>(val gen: () -> T) {
    private val extraData = WeakHashMap<Any, T>()

    operator fun getValue(instance: Any, property: KProperty<*>): T {
        return extraData.getOrPut(instance) { gen() }
    }

    operator fun setValue(instance: Any, property: KProperty<*>, value: T) {
        extraData.put(instance, value)
    }
}
