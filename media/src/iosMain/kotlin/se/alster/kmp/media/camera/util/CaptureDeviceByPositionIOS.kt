package se.alster.kmp.media.camera.util

import kotlinx.cinterop.ExperimentalForeignApi
import platform.AVFoundation.AVCaptureDevice
import platform.AVFoundation.AVCaptureDeviceInput
import platform.AVFoundation.AVMediaTypeVideo
import platform.AVFoundation.position
import se.alster.kmp.media.camera.CameraFacing
import se.alster.kmp.media.camera.extensions.toAVCaptureDevicePosition

@OptIn(ExperimentalForeignApi::class)
fun captureDeviceInputByPosition(position: CameraFacing): AVCaptureDeviceInput? =
    AVCaptureDevice.devicesWithMediaType(AVMediaTypeVideo)
        .filterIsInstance<AVCaptureDevice>()
        .firstOrNull { it.position == position.toAVCaptureDevicePosition() }?.let {
            AVCaptureDeviceInput(device = it, error = null)
        }

