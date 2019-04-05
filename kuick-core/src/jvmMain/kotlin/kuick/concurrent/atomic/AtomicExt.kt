package kuick.concurrent.atomic

import java.util.concurrent.atomic.*
import kotlin.reflect.*

operator fun AtomicBoolean.setValue(container: Any, property: KProperty<*>, value: Boolean) = this.set(value)
operator fun AtomicBoolean.getValue(container: Any, property: KProperty<*>): Boolean = this.get()

operator fun AtomicInteger.setValue(container: Any, property: KProperty<*>, value: Int) = this.set(value)
operator fun AtomicInteger.getValue(container: Any, property: KProperty<*>): Int = this.get()

operator fun AtomicLong.setValue(container: Any, property: KProperty<*>, value: Long) = this.set(value)
operator fun AtomicLong.getValue(container: Any, property: KProperty<*>): Long = this.get()

operator fun <T> AtomicReference<T>.setValue(container: Any, property: KProperty<*>, value: T) = this.set(value)
operator fun <T> AtomicReference<T>.getValue(container: Any, property: KProperty<*>): T = this.get()
