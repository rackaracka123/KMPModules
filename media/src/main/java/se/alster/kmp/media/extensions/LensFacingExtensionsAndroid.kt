package se.alster.kmp.media.extensions

import androidx.camera.core.CameraSelector
import se.alster.kmp.media.camera.CameraFacing

internal fun CameraFacing.toLensFacing() = when(this) {
    CameraFacing.Back -> CameraSelector.LENS_FACING_BACK
    CameraFacing.Front -> CameraSelector.LENS_FACING_FRONT
}
