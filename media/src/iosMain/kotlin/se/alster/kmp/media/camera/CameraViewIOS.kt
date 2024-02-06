package se.alster.kmp.media.camera

import androidx.compose.foundation.layout.Box
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.interop.UIKitView
import androidx.compose.ui.text.TextStyle
import kotlinx.cinterop.CValue
import kotlinx.cinterop.ExperimentalForeignApi
import platform.AVFoundation.AVAuthorizationStatusAuthorized
import platform.AVFoundation.AVAuthorizationStatusDenied
import platform.AVFoundation.AVAuthorizationStatusNotDetermined
import platform.AVFoundation.AVAuthorizationStatusRestricted
import platform.AVFoundation.AVCaptureDevice
import platform.AVFoundation.AVLayerVideoGravity
import platform.AVFoundation.AVMediaTypeVideo
import platform.AVFoundation.authorizationStatusForMediaType
import platform.AVFoundation.requestAccessForMediaType
import platform.CoreGraphics.CGRect
import platform.QuartzCore.CATransaction
import platform.QuartzCore.kCATransactionDisableActions
import platform.UIKit.UIDevice
import platform.UIKit.UIView
import se.alster.kmp.media.AspectRatio
import se.alster.kmp.media.camera.exception.CameraNotFoundException
import se.alster.kmp.media.toAVLayerVideoGravity

@Composable
actual fun CameraView(
    modifier: Modifier,
    aspectRatio: AspectRatio,
    onQrCodeScanned: ((String) -> Unit)?,
    takePhotoController: ((onTakePhoto: ((photo: CaptureResult) -> Unit) -> Unit) -> Unit)?,
    cameraFacing: CameraFacing
) {
    CameraViewIOS(
        modifier,
        aspectRatio.toAVLayerVideoGravity(),
        onQrCodeScanned = onQrCodeScanned,
        photoController = takePhotoController,
        cameraFacing = cameraFacing
    )
}

@OptIn(ExperimentalForeignApi::class)
@Composable
private fun CameraViewIOS(
    modifier: Modifier,
    videoGravity: AVLayerVideoGravity,
    onQrCodeScanned: ((String) -> Unit)?,
    photoController: ((onTakePhoto: ((photo: CaptureResult) -> Unit) -> Unit) -> Unit)?,
    cameraFacing: CameraFacing
) {
    var cameraState: CameraState by remember { mutableStateOf(CameraState.Undefined) }

    LaunchedEffect(Unit) {
        when (AVCaptureDevice.authorizationStatusForMediaType(AVMediaTypeVideo)) {
            AVAuthorizationStatusAuthorized -> {
                cameraState = CameraState.Access.Authorized
            }

            AVAuthorizationStatusDenied, AVAuthorizationStatusRestricted -> {
                cameraState = CameraState.Access.Denied
            }

            AVAuthorizationStatusNotDetermined -> {
                AVCaptureDevice.requestAccessForMediaType(
                    mediaType = AVMediaTypeVideo
                ) { success ->
                    cameraState =
                        if (success) CameraState.Access.Authorized else CameraState.Access.Denied
                }
            }
        }
    }
    when (cameraState) {
        CameraState.Undefined -> {
            // Waiting for the user to accept permission
        }

        CameraState.Access.Denied -> {
            Box(modifier, contentAlignment = Alignment.Center) {
                Text("Camera access denied", color = Color.Black, style = TextStyle.Default)
            }
        }

        CameraState.Simulator -> {
            Box(modifier, contentAlignment = Alignment.Center) {
                Text(
                    "Camera not available in simulator",
                    color = Color.Black,
                    style = TextStyle.Default
                )
            }
        }

        CameraState.Access.Authorized -> {
            val cameraViewController =
                remember {
                    try {
                        CameraViewControllerIOS(
                            videoGravity,
                            photoController,
                            onQrCodeScanned,
                        )
                    } catch (e: CameraNotFoundException) {
                        null
                    }
                }
            if (cameraViewController == null) {
                cameraState = CameraState.Simulator
            }
            cameraViewController?.let {
                LaunchedEffect(cameraFacing) {
                    cameraViewController.onCameraFacingChanged(cameraFacing)
                }
                LaunchedEffect(UIDevice.currentDevice.orientation) {
                    cameraViewController.onOrientationChanged(UIDevice.currentDevice.orientation)
                }
                DisposableEffect(Unit) {
                    UIDevice.currentDevice.beginGeneratingDeviceOrientationNotifications()
                    onDispose {
                        UIDevice.currentDevice.endGeneratingDeviceOrientationNotifications()
                        cameraViewController.onDispose()
                    }
                }
                UIKitView(factory = {
                    cameraViewController.view
                }, modifier = modifier,
                    onResize = { view: UIView, rect: CValue<CGRect> ->
                        CATransaction.begin()
                        CATransaction.setValue(true, kCATransactionDisableActions)
                        view.layer.setFrame(rect)
                        cameraViewController.onResize(rect)
                        CATransaction.commit()
                    })
            }
        }
    }
}
