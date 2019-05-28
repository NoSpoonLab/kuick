package kuick.time

import kotlin.js.Date

actual object SimpleClock : Clock {
    actual override fun nowDouble(): Double = Date.now()
}