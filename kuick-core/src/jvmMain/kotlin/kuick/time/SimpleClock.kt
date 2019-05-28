package kuick.time

actual object SimpleClock : Clock {
    actual override fun nowDouble(): Double = System.currentTimeMillis().toDouble()
}
