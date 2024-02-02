package se.alster.kmp.media.camera.extensions

import platform.AVFoundation.AVCaptureDevicePositionBack
import platform.AVFoundation.AVCaptureDevicePositionFront
import se.alster.kmp.media.camera.CameraFacing

internal fun CameraFacing.toAVCaptureDevicePosition() =
    when (this) {
        CameraFacing.Back -> AVCaptureDevicePositionBack
        CameraFacing.Front -> AVCaptureDevicePositionFront
    }
