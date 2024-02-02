package se.alster.kmp.media.extensions

import java.nio.ByteBuffer

fun ByteBuffer.moveToByteArray(): ByteArray {
    val array = ByteArray(remaining())
    get(array)
    return array
}
