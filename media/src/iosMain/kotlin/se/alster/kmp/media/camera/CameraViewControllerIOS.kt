package se.alster.kmp.media.camera

import androidx.compose.ui.graphics.ImageBitmap
import kotlinx.cinterop.CValue
import kotlinx.cinterop.ExperimentalForeignApi
import platform.AVFoundation.AVCaptureConnection
import platform.AVFoundation.AVCaptureDevice
import platform.AVFoundation.AVCaptureDeviceInput
import platform.AVFoundation.AVCaptureMetadataOutput
import platform.AVFoundation.AVCaptureMetadataOutputObjectsDelegateProtocol
import platform.AVFoundation.AVCapturePhoto
import platform.AVFoundation.AVCapturePhotoCaptureDelegateProtocol
import platform.AVFoundation.AVCapturePhotoOutput
import platform.AVFoundation.AVCapturePhotoSettings
import platform.AVFoundation.AVCaptureSession
import platform.AVFoundation.AVCaptureVideoOrientationLandscapeRight
import platform.AVFoundation.AVCaptureVideoPreviewLayer
import platform.AVFoundation.AVLayerVideoGravity
import platform.AVFoundation.AVLayerVideoGravityResizeAspectFill
import platform.AVFoundation.AVMediaTypeVideo
import platform.AVFoundation.AVMetadataMachineReadableCodeObject
import platform.AVFoundation.AVMetadataObjectTypeQRCode
import platform.AVFoundation.AVVideoCodecKey
import platform.AVFoundation.AVVideoCodecTypeJPEG
import platform.AVFoundation.fileDataRepresentation
import platform.AudioToolbox.AudioServicesPlaySystemSound
import platform.AudioToolbox.kSystemSoundID_Vibrate
import platform.CoreGraphics.CGRect
import platform.Foundation.NSError
import platform.UIKit.UIColor
import platform.UIKit.UIDevice
import platform.UIKit.UIImage
import platform.UIKit.UIViewController
import platform.darwin.NSObject
import platform.darwin.dispatch_get_main_queue
import se.alster.kmp.media.toImageBitmap

internal class CameraViewControllerIOS(
    private val videoGravity: AVLayerVideoGravity,
    private val onTakePhoto: ((onTakePhoto: ((photo: ImageBitmap) -> Unit) -> Unit) -> Unit)?,
    private val onScanComplete: ((String) -> Unit)?
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

        onTakePhoto?.invoke { callback ->
            val capturePhotoOutput = AVCapturePhotoOutput()
            capturePhotoOutput.capturePhotoWithSettings(
                AVCapturePhotoSettings.photoSettingsWithFormat(
                    format = mapOf(AVVideoCodecKey to AVVideoCodecTypeJPEG)
                ),
                object : NSObject(), AVCapturePhotoCaptureDelegateProtocol {
                    override fun captureOutput(
                        output: AVCapturePhotoOutput,
                        didFinishProcessingPhoto: AVCapturePhoto,
                        error: NSError?
                    ) {
                        didFinishProcessingPhoto.fileDataRepresentation()?.let {
                            callback(UIImage(it).toImageBitmap())
                        }
                    }
                },
            )
            captureSession.addOutput(capturePhotoOutput)
        }


        previewLayer.frame = view.layer.bounds
        previewLayer.videoGravity = videoGravity
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

        onScanComplete?.invoke(stringValue)
        dismissViewControllerAnimated(true, null)
    }

    override fun prefersStatusBarHidden(): Boolean {
        return false
    }
}
