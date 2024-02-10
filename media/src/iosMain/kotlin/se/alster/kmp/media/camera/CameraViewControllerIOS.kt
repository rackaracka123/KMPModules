package se.alster.kmp.media.camera

import kotlinx.cinterop.CValue
import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.cinterop.ObjCAction
import platform.AVFoundation.AVCaptureConnection
import platform.AVFoundation.AVCaptureDeviceInput
import platform.AVFoundation.AVCaptureMetadataOutput
import platform.AVFoundation.AVCaptureMetadataOutputObjectsDelegateProtocol
import platform.AVFoundation.AVCaptureVideoOrientationLandscapeLeft
import platform.AVFoundation.AVCaptureVideoOrientationLandscapeRight
import platform.AVFoundation.AVCaptureVideoOrientationPortrait
import platform.AVFoundation.AVLayerVideoGravity
import platform.AVFoundation.AVMetadataMachineReadableCodeObject
import platform.AVFoundation.AVMetadataObjectTypeQRCode
import platform.AudioToolbox.AudioServicesPlaySystemSound
import platform.AudioToolbox.kSystemSoundID_Vibrate
import platform.CoreGraphics.CGRect
import platform.Foundation.NSNotification
import platform.UIKit.UIColor
import platform.UIKit.UIDevice
import platform.UIKit.UIDeviceOrientation
import platform.UIKit.UIViewController
import platform.darwin.NSObject
import platform.darwin.dispatch_get_main_queue

/*
 * CameraViewControllerIOS is a UIViewController that manages the camera view and the camera session.
 * It is used to take photos and scan QR codes.
 * It throws a CameraNotFoundException if the front or back camera is not found.
 */
internal class CameraViewControllerIOS(
    private val videoGravity: AVLayerVideoGravity,
    captureController: CaptureController?,
    private val onScanComplete: ((String) -> Unit)?,
) : UIViewController(nibName = null, bundle = null) {
    private val captureControllerIOS = captureController as CaptureControllerIOS

    @OptIn(ExperimentalForeignApi::class)
    fun onResize(rect: CValue<CGRect>) {
        captureControllerIOS.previewLayer.setFrame(rect)
    }

    fun onDispose() {
        captureControllerIOS.captureSession.stopRunning()
    }

    @OptIn(ExperimentalForeignApi::class)
    override fun viewDidLoad() {
        super.viewDidLoad()

        view.backgroundColor = UIColor.blackColor
        captureControllerIOS.previewLayer.frame = view.layer.bounds
        captureControllerIOS.previewLayer.videoGravity = videoGravity
        view.layer.addSublayer(captureControllerIOS.previewLayer)

        captureControllerIOS.captureSession.startRunning()

        switchCamera(CameraFacing.Back)

        val metadataOutput = AVCaptureMetadataOutput()

        if (metadataOutput.availableMetadataObjectTypes.contains(AVMetadataObjectTypeQRCode)
            && captureControllerIOS.captureSession.canAddOutput(metadataOutput)
            && onScanComplete != null
        ) {
            captureControllerIOS.captureSession.addOutput(metadataOutput)
            metadataOutput.setMetadataObjectsDelegate(
                object : NSObject(), AVCaptureMetadataOutputObjectsDelegateProtocol {
                    override fun captureOutput(
                        output: platform.AVFoundation.AVCaptureOutput,
                        didOutputMetadataObjects: List<*>,
                        fromConnection: AVCaptureConnection
                    ) {
                        captureControllerIOS.captureSession.stopRunning()
                        val data = didOutputMetadataObjects.first()
                        val readableObject = data as? AVMetadataMachineReadableCodeObject
                        val stringValue = readableObject?.stringValue!!
                        AudioServicesPlaySystemSound(kSystemSoundID_Vibrate)

                        onScanComplete.invoke(stringValue)
                        dismissViewControllerAnimated(true, null)
                    }
                }, queue = dispatch_get_main_queue()
            )
            metadataOutput.metadataObjectTypes = listOf(AVMetadataObjectTypeQRCode)
        }

    }

    override fun viewWillAppear(animated: Boolean) {
        super.viewWillAppear(animated)
        if (!captureControllerIOS.captureSession.isRunning()) {
            captureControllerIOS.captureSession.startRunning()
        }
    }

    override fun viewWillDisappear(animated: Boolean) {
        super.viewWillDisappear(animated)
        if (captureControllerIOS.captureSession.isRunning()) {
            captureControllerIOS.captureSession.stopRunning()
        }
    }

    override fun prefersStatusBarHidden(): Boolean {
        return false
    }

    fun onCameraFacingChanged(cameraFacing: CameraFacing) {
        switchCamera(cameraFacing)
    }

    @Suppress("UNUSED_PARAMETER")
    @ObjCAction
    fun orientationDidChange(arg: NSNotification) {
        val cameraConnection = captureControllerIOS.previewLayer.connection
        if (cameraConnection != null) {
            captureControllerIOS.actualOrientation = when (UIDevice.currentDevice.orientation) {
                UIDeviceOrientation.UIDeviceOrientationPortrait ->
                    AVCaptureVideoOrientationPortrait

                UIDeviceOrientation.UIDeviceOrientationLandscapeLeft ->
                    AVCaptureVideoOrientationLandscapeRight

                UIDeviceOrientation.UIDeviceOrientationLandscapeRight ->
                    AVCaptureVideoOrientationLandscapeLeft

                UIDeviceOrientation.UIDeviceOrientationPortraitUpsideDown ->
                    AVCaptureVideoOrientationPortrait

                else -> cameraConnection.videoOrientation
            }
            cameraConnection.videoOrientation = captureControllerIOS.actualOrientation
        }
    }

    private fun switchCamera(cameraFacing: CameraFacing) {
        removeAllCameras()
        when (cameraFacing) {
            CameraFacing.Front -> {
                captureControllerIOS.captureSession.addInput(captureControllerIOS.frontCamera)
            }

            CameraFacing.Back -> {
                captureControllerIOS.captureSession.addInput(captureControllerIOS.backCamera)
            }
        }
    }

    private fun removeAllCameras() {
        captureControllerIOS.captureSession.inputs.forEach {
            it as AVCaptureDeviceInput
            captureControllerIOS.captureSession.removeInput(it)
        }
    }
}
