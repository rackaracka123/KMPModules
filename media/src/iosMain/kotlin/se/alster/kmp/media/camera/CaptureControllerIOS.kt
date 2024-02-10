package se.alster.kmp.media.camera

import platform.AVFoundation.AVCaptureDeviceInput
import platform.AVFoundation.AVCaptureMovieFileOutput
import platform.AVFoundation.AVCapturePhoto
import platform.AVFoundation.AVCapturePhotoCaptureDelegateProtocol
import platform.AVFoundation.AVCapturePhotoOutput
import platform.AVFoundation.AVCapturePhotoSettings
import platform.AVFoundation.AVCaptureSession
import platform.AVFoundation.AVCaptureVideoOrientation
import platform.AVFoundation.AVCaptureVideoOrientationLandscapeRight
import platform.AVFoundation.AVCaptureVideoPreviewLayer
import platform.AVFoundation.AVMediaTypeVideo
import platform.AVFoundation.AVVideoCodecKey
import platform.AVFoundation.AVVideoCodecTypeJPEG
import platform.AVFoundation.fileDataRepresentation
import platform.Foundation.NSError
import platform.UIKit.UIImage
import platform.darwin.NSObject
import se.alster.kmp.media.camera.exception.CameraNotFoundException
import se.alster.kmp.media.camera.util.captureDeviceInputByPosition
import se.alster.kmp.media.toImageBitmap
import se.alster.kmp.storage.FilePath
import se.alster.kmp.storage.toNSURL

class CaptureControllerIOS : CaptureController {
    internal val captureSession: AVCaptureSession = AVCaptureSession()
    internal val previewLayer: AVCaptureVideoPreviewLayer =
        AVCaptureVideoPreviewLayer(session = captureSession)

    internal var actualOrientation: AVCaptureVideoOrientation =
        AVCaptureVideoOrientationLandscapeRight

    private val capturePhotoOutput = AVCapturePhotoOutput()
    private val captureVideoFileOutput = AVCaptureMovieFileOutput()
    private val cameraCaptureFileOutputRecordingDelegateIOS =
        CameraCaptureFileOutputRecordingDelegateIOS()

    internal val frontCamera: AVCaptureDeviceInput =
        captureDeviceInputByPosition(CameraFacing.Front)
            ?: throw CameraNotFoundException("Front camera not found")
    internal val backCamera: AVCaptureDeviceInput = captureDeviceInputByPosition(CameraFacing.Back)
        ?: throw CameraNotFoundException("Back camera not found")

    init {
        if (captureSession.canAddOutput(capturePhotoOutput)) {
            captureSession.addOutput(capturePhotoOutput)
        }
        if (captureSession.canAddOutput(captureVideoFileOutput)) {
            captureSession.addOutput(captureVideoFileOutput)
        }
        captureVideoFileOutput.connectionWithMediaType(AVMediaTypeVideo)?.videoOrientation =
            actualOrientation
        capturePhotoOutput.connectionWithMediaType(AVMediaTypeVideo)?.videoOrientation =
            actualOrientation
    }

    override fun takePicture(callback: (photo: CaptureResult) -> Unit) {
        capturePhotoOutput.capturePhotoWithSettings(
            AVCapturePhotoSettings.photoSettingsWithFormat(
                format = mapOf(AVVideoCodecKey to AVVideoCodecTypeJPEG)
            ), delegate = object : NSObject(), AVCapturePhotoCaptureDelegateProtocol {
                override fun captureOutput(
                    output: AVCapturePhotoOutput,
                    didFinishProcessingPhoto: AVCapturePhoto,
                    error: NSError?
                ) {
                    didFinishProcessingPhoto.fileDataRepresentation()?.let {
                        return callback(CaptureResult.Success(UIImage(it).toImageBitmap()))
                    }
                    if (error != null) {
                        return callback(CaptureResult.Failure)
                    }
                }
            }
        )
    }

    override fun startRecording(filepath: FilePath) {
        println("Recording started to: $filepath")
        captureVideoFileOutput.startRecordingToOutputFileURL(
            outputFileURL = filepath.toNSURL(),
            recordingDelegate = cameraCaptureFileOutputRecordingDelegateIOS
        )
    }

    override fun stopRecording() {
        println("Recording stopped")
        captureVideoFileOutput.stopRecording()
    }
}
