package kuick.utils

import java.security.*

fun ByteArray.md5() = MessageDigest.getInstance("MD5").digest(this)
fun ByteArray.sha1() = MessageDigest.getInstance("SHA-1").digest(this)
fun ByteArray.sha256() = MessageDigest.getInstance("SHA-256").digest(this)
fun ByteArray.sha512() = MessageDigest.getInstance("SHA-512").digest(this)
