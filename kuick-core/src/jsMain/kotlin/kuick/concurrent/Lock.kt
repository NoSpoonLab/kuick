package kuick.concurrent

actual class Lock {
    actual inline operator fun <T> invoke(callback: () -> T): T = callback()
}