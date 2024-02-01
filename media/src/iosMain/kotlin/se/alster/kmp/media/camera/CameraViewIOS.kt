package se.alster.kmp.media.camera

import androidx.compose.foundation.layout.Box
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.interop.UIKitView
import androidx.compose.ui.text.TextStyle
import kotlinx.cinterop.CValue
import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.coroutines.CoroutineScope
import platform.AVFoundation.AVAuthorizationStatusAuthorized
import platform.AVFoundation.AVAuthorizationStatusDenied
import platform.AVFoundation.AVAuthorizationStatusNotDetermined
import platform.AVFoundation.AVAuthorizationStatusRestricted
import platform.AVFoundation.AVCaptureDevice
import platform.AVFoundation.AVCapturePhotoOutput
import platform.AVFoundation.AVMediaTypeVideo
import platform.AVFoundation.authorizationStatusForMediaType
import platform.AVFoundation.requestAccessForMediaType
import platform.CoreGraphics.CGRect
import platform.QuartzCore.CATransaction
import platform.QuartzCore.kCATransactionDisableActions
import platform.UIKit.UIDevice
import platform.UIKit.UIView

@Composable
actual fun CameraView(modifier: Modifier) {
    CameraViewIOS(modifier, onQrCodeScanned = {})
}


private sealed interface CameraAccess {
    data object Undefined : CameraAccess
    data object Denied : CameraAccess
    data object Authorized : CameraAccess
}

@OptIn(ExperimentalForeignApi::class)
@Composable
private fun CameraViewIOS(modifier: Modifier, onQrCodeScanned: (String) -> Unit) {
    var cameraAccess: CameraAccess by remember { mutableStateOf(CameraAccess.Undefined) }
    val waitForCameraScope: CoroutineScope = rememberCoroutineScope()

    LaunchedEffect(Unit) {
        when (AVCaptureDevice.authorizationStatusForMediaType(AVMediaTypeVideo)) {
            AVAuthorizationStatusAuthorized -> {
                cameraAccess = CameraAccess.Authorized
            }

            AVAuthorizationStatusDenied, AVAuthorizationStatusRestricted -> {
                cameraAccess = CameraAccess.Denied
            }

            AVAuthorizationStatusNotDetermined -> {
                AVCaptureDevice.requestAccessForMediaType(
                    mediaType = AVMediaTypeVideo
                ) { success ->
                    cameraAccess = if (success) CameraAccess.Authorized else CameraAccess.Denied
                }
            }
        }
    }
    when (cameraAccess) {
        CameraAccess.Undefined -> {
            // Waiting for the user to accept permission
        }

        CameraAccess.Denied -> {
            Box(modifier, contentAlignment = Alignment.Center) {
                Text("Camera access denied", color = Color.Black, style = TextStyle.Default)
            }
        }

        CameraAccess.Authorized -> {
            val cameraViewController =
                remember {
                    CameraViewControllerIOS(
                        { takePhoto ->
                            takePhoto {
                                println("Photo taken")
                                println("Size: ${it.height}")
                            }
                        },
                        onQrCodeScanned
                    )
                }

            LaunchedEffect(UIDevice.currentDevice.orientation) {
                cameraViewController.onOrientationChanged()
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
