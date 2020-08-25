package kuick.utils

import org.khronos.webgl.Int8Array
import kotlin.browser.window

fun randomBytes(count: Int): ByteArray {
    val data = Int8Array(count)
    window.asDynamic().crypto.getRandomValues(data)
    return data.unsafeCast<ByteArray>()
}

actual fun randomUUID(): String {
    return "${randomBytes(4).hex}-${randomBytes(2).hex}-${randomBytes(2).hex}-${randomBytes(2).hex}--${randomBytes(6).hex}"
}
