package kuick.concurrent

expect class Lock() {
    inline operator fun <T> invoke(callback: () -> T): T
}