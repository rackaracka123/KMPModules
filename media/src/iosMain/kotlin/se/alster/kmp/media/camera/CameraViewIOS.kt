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
import platform.AVFoundation.AVCaptureConnection
import platform.AVFoundation.AVCaptureDevice
import platform.AVFoundation.AVCaptureDeviceInput
import platform.AVFoundation.AVCaptureMetadataOutput
import platform.AVFoundation.AVCaptureMetadataOutputObjectsDelegateProtocol
import platform.AVFoundation.AVCaptureSession
import platform.AVFoundation.AVCaptureVideoOrientation
import platform.AVFoundation.AVCaptureVideoOrientationLandscapeLeft
import platform.AVFoundation.AVCaptureVideoOrientationLandscapeRight
import platform.AVFoundation.AVCaptureVideoOrientationPortrait
import platform.AVFoundation.AVCaptureVideoOrientationPortraitUpsideDown
import platform.AVFoundation.AVCaptureVideoPreviewLayer
import platform.AVFoundation.AVLayerVideoGravityResizeAspectFill
import platform.AVFoundation.AVMediaTypeVideo
import platform.AVFoundation.AVMetadataMachineReadableCodeObject
import platform.AVFoundation.AVMetadataObjectTypeQRCode
import platform.AVFoundation.authorizationStatusForMediaType
import platform.AVFoundation.requestAccessForMediaType
import platform.AudioToolbox.AudioServicesPlaySystemSound
import platform.AudioToolbox.kSystemSoundID_Vibrate
import platform.CoreGraphics.CGRect
import platform.QuartzCore.CATransaction
import platform.QuartzCore.kCATransactionDisableActions
import platform.UIKit.UIColor
import platform.UIKit.UIDevice
import platform.UIKit.UIDeviceOrientation
import platform.UIKit.UIView
import platform.UIKit.UIViewController
import platform.darwin.dispatch_get_main_queue

@Composable
actual fun CameraView(modifier: Modifier) {
    QrScannerScreen(modifier, onQrCodeScanned = {})
}


private sealed interface CameraAccess {
    data object Undefined : CameraAccess
    data object Denied : CameraAccess
    data object Authorized : CameraAccess
}

@OptIn(ExperimentalForeignApi::class)
@Composable
private fun QrScannerScreen(modifier: Modifier, onQrCodeScanned: (String) -> Unit) {
    var cameraAccess: CameraAccess by remember { mutableStateOf(CameraAccess.Undefined) }

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
                Text("Camera access denied", color = Color.White, style = TextStyle.Default)
            }
        }

        CameraAccess.Authorized -> {
            val scannerController = remember { QRScannerViewController(onQrCodeScanned) }

            LaunchedEffect(UIDevice.currentDevice.orientation) {
                scannerController.onOrientationChanged()
            }
            DisposableEffect(Unit) {
                UIDevice.currentDevice.beginGeneratingDeviceOrientationNotifications()
                onDispose {
                    UIDevice.currentDevice.endGeneratingDeviceOrientationNotifications()
                    scannerController.onDispose()
                }
            }
            UIKitView(factory = {
                scannerController.view
            }, modifier = modifier,
                onResize = { view: UIView, rect: CValue<CGRect> ->
                    CATransaction.begin()
                    CATransaction.setValue(true, kCATransactionDisableActions)
                    view.layer.setFrame(rect)
                    scannerController.onResize(rect)
                    CATransaction.commit()
                })
        }
    }
}

private class QRScannerViewController(
    private val onScanComplete: (String) -> Unit
) :
    UIViewController(nibName = null, bundle = null),
    AVCaptureMetadataOutputObjectsDelegateProtocol {

    private val captureSession: AVCaptureSession = AVCaptureSession()
    private val previewLayer: AVCaptureVideoPreviewLayer =
        AVCaptureVideoPreviewLayer(session = captureSession)

    fun onOrientationChanged() {
        if (previewLayer.connection?.videoOrientation != null) {
            previewLayer.connection?.videoOrientation = UIDevice.currentDevice.orientation
                .mapAVCaptureVideoOrientation(AVCaptureVideoOrientationLandscapeRight)
        }
    }

    @OptIn(ExperimentalForeignApi::class)
    fun onResize(rect: CValue<CGRect>) {
        previewLayer.setFrame(rect)
    }

    fun onDispose() {
        captureSession.stopRunning()
    }

    @OptIn(ExperimentalForeignApi::class)
    override fun viewDidLoad() {
        super.viewDidLoad()

        view.backgroundColor = UIColor.blackColor

        val videoCaptureDevice =
            AVCaptureDevice.defaultDeviceWithMediaType(mediaType = AVMediaTypeVideo) ?: return

        val videoInput: AVCaptureDeviceInput = try {
            AVCaptureDeviceInput(device = videoCaptureDevice, error = null)
        } catch (e: Exception) {
            return
        }

        if (captureSession.canAddInput(videoInput)) {
            captureSession.addInput(videoInput)
        } else {
            return
        }

        val metadataOutput = AVCaptureMetadataOutput()

        if (captureSession.canAddOutput(metadataOutput)) {
            captureSession.addOutput(metadataOutput)
            metadataOutput.setMetadataObjectsDelegate(this, queue = dispatch_get_main_queue())
            metadataOutput.metadataObjectTypes = listOf(AVMetadataObjectTypeQRCode)
        } else {
            return
        }

        previewLayer.frame = view.layer.bounds
        previewLayer.videoGravity = AVLayerVideoGravityResizeAspectFill
        view.layer.addSublayer(previewLayer)

        captureSession.startRunning()
    }

    override fun viewWillAppear(animated: Boolean) {
        super.viewWillAppear(animated)
        if (!captureSession.isRunning()) {
            captureSession.startRunning()
        }
    }

    override fun viewWillDisappear(animated: Boolean) {
        super.viewWillDisappear(animated)
        if (captureSession.isRunning()) {
            captureSession.stopRunning()
        }
    }


    override fun captureOutput(
        output: platform.AVFoundation.AVCaptureOutput,
        didOutputMetadataObjects: List<*>,
        fromConnection: AVCaptureConnection
    ) {
        captureSession.stopRunning()
        val data = didOutputMetadataObjects.first()
        val readableObject = data as? AVMetadataMachineReadableCodeObject
        val stringValue = readableObject?.stringValue!!
        AudioServicesPlaySystemSound(kSystemSoundID_Vibrate)

        onScanComplete(stringValue)
        dismissViewControllerAnimated(true, null)
    }

    override fun prefersStatusBarHidden(): Boolean {
        return false
    }
}

private fun UIDeviceOrientation.mapAVCaptureVideoOrientation(
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
