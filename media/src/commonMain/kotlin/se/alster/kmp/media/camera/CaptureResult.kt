package se.alster.kmp.media.camera

import androidx.compose.ui.graphics.ImageBitmap

sealed class CaptureResult {
    data class Success(val bitmap: ImageBitmap) : CaptureResult()
    data object Failure: CaptureResult()

    fun imageOrNull() = if (this is Success) {
        bitmap
    } else {
        null
    }
}
