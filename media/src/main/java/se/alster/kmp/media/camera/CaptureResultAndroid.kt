package se.alster.kmp.media.camera

import androidx.compose.ui.graphics.ImageBitmap

sealed class CaptureResultAndroid {
    data object Loading : CaptureResultAndroid()
    data class Success(val bitmap: ImageBitmap) : CaptureResultAndroid()
    data object Failure: CaptureResultAndroid()

    fun imageOrNull() = if (this is Success) {
        bitmap
    } else {
        null
    }
}
