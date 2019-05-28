package kuick.time

interface Clock {
    fun nowDouble(): Double
}

fun Clock(callback: () -> Double) = object : Clock {
    override fun nowDouble(): Double = callback()
}

expect object SimpleClock : Clock {
    override fun nowDouble(): Double
}

fun Clock.now(): Long = nowDouble().toLong()
