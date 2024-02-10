package se.alster.kmp.media.camera

import androidx.compose.ui.graphics.ImageBitmap

sealed class ImageCaptureResult {
    data class Success(val bitmap: ImageBitmap) : ImageCaptureResult()
    data object Failure: ImageCaptureResult()

    fun imageOrNull() = if (this is Success) {
        bitmap
    } else {
        null
    }
}
