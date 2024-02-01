package se.alster.kmp.media.extensions

import android.graphics.BitmapFactory
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asImageBitmap
import android.graphics.Bitmap as AndroidBitmap

fun ByteArray.toImageBitmap(): ImageBitmap = toAndroidBitmap().asImageBitmap()

private fun ByteArray.toAndroidBitmap(): AndroidBitmap {
    return BitmapFactory.decodeByteArray(this, 0, size)
}
