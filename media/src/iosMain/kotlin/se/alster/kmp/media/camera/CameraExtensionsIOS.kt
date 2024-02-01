package se.alster.kmp.media.camera

import platform.AVFoundation.AVCaptureVideoOrientation
import platform.AVFoundation.AVCaptureVideoOrientationLandscapeLeft
import platform.AVFoundation.AVCaptureVideoOrientationLandscapeRight
import platform.AVFoundation.AVCaptureVideoOrientationPortrait
import platform.AVFoundation.AVCaptureVideoOrientationPortraitUpsideDown
import platform.AVFoundation.AVLayerVideoGravity
import platform.UIKit.UIDeviceOrientation

internal fun UIDeviceOrientation.mapAVCaptureVideoOrientation(
    default: AVCaptureVideoOrientation
): AVCaptureVideoOrientation = when (this) {
    UIDeviceOrientation.UIDeviceOrientationPortrait ->
        AVCaptureVideoOrientationPortrait

    UIDeviceOrientation.UIDeviceOrientationLandscapeLeft ->
        AVCaptureVideoOrientationLandscapeRight

    UIDeviceOrientation.UIDeviceOrientationLandscapeRight ->
        AVCaptureVideoOrientationLandscapeLeft

    UIDeviceOrientation.UIDeviceOrientationPortraitUpsideDown ->
        AVCaptureVideoOrientationPortraitUpsideDown

    else -> default
}
