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
import platform.Foundation.NSNotificationCenter
import platform.Foundation.NSSelectorFromString
import platform.QuartzCore.CATransaction
import platform.QuartzCore.kCATransactionDisableActions
import platform.UIKit.UIView
import se.alster.kmp.media.AspectRatio
import se.alster.kmp.media.camera.exception.CameraNotFoundException
import se.alster.kmp.media.toAVLayerVideoGravity

@Composable
actual fun CameraView(
    modifier: Modifier,
    aspectRatio: AspectRatio,
    onQrCodeScanned: ((String) -> Unit)?,
    captureController: CaptureController?,
    cameraFacing: CameraFacing
) {
    CameraViewIOS(
        modifier,
        aspectRatio.toAVLayerVideoGravity(),
        onQrCodeScanned = onQrCodeScanned,
        captureController = captureController,
        cameraFacing = cameraFacing
    )
}

@OptIn(ExperimentalForeignApi::class)
@Composable
private fun CameraViewIOS(
    modifier: Modifier,
    videoGravity: AVLayerVideoGravity,
    onQrCodeScanned: ((String) -> Unit)?,
    captureController: CaptureController?,
    cameraFacing: CameraFacing
) {
    var cameraState: CameraStateIOS by remember { mutableStateOf(CameraStateIOS.Undefined) }

    LaunchedEffect(Unit) {
        when (AVCaptureDevice.authorizationStatusForMediaType(AVMediaTypeVideo)) {
            AVAuthorizationStatusAuthorized -> {
                cameraState = CameraStateIOS.Access.Authorized
            }

            AVAuthorizationStatusDenied, AVAuthorizationStatusRestricted -> {
                cameraState = CameraStateIOS.Access.Denied
            }

            AVAuthorizationStatusNotDetermined -> {
                AVCaptureDevice.requestAccessForMediaType(
                    mediaType = AVMediaTypeVideo
                ) { success ->
                    cameraState =
                        if (success) CameraStateIOS.Access.Authorized else CameraStateIOS.Access.Denied
                }
            }
        }
    }
    when (cameraState) {
        CameraStateIOS.Undefined -> {
            // Waiting for the user to accept permission
        }

        CameraStateIOS.Access.Denied -> {
            Box(modifier, contentAlignment = Alignment.Center) {
                Text("Camera access denied", color = Color.Black, style = TextStyle.Default)
            }
        }

        CameraStateIOS.Simulator -> {
            Box(modifier, contentAlignment = Alignment.Center) {
                Text(
                    "Camera not available in simulator",
                    color = Color.Black,
                    style = TextStyle.Default
                )
            }
        }

        CameraStateIOS.Access.Authorized -> {
            val cameraViewController =
                remember {
                    try {
                        CameraViewControllerIOS(
                            videoGravity,
                            captureController,
                            onQrCodeScanned,
                        )
                    } catch (e: CameraNotFoundException) {
                        null
                    }
                }
            if (cameraViewController == null) {
                cameraState = CameraStateIOS.Simulator
            }
            cameraViewController?.let {
                LaunchedEffect(cameraFacing) {
                    cameraViewController.onCameraFacingChanged(cameraFacing)
                }
                DisposableEffect(Unit) {
                    val notificationName = platform.UIKit.UIDeviceOrientationDidChangeNotification
                    NSNotificationCenter.defaultCenter.addObserver(
                        observer = it,
                        selector = NSSelectorFromString(
                            CameraViewControllerIOS::orientationDidChange.name + ":"
                        ),
                        name = notificationName,
                        `object` = null
                    )
                    onDispose {
                        cameraViewController.onDispose()
                        NSNotificationCenter.defaultCenter.removeObserver(
                            observer = it,
                            name = notificationName,
                            `object` = null
                        )
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
