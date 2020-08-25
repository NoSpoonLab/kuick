package kuick.time

import kotlin.js.Date

actual fun nowUnix(): Long = Date.now().toLong()
