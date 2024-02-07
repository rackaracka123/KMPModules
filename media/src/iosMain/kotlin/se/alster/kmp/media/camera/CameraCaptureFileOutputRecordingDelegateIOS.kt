package se.alster.kmp.media.camera


import kotlinx.cinterop.ExperimentalForeignApi
import platform.AVFoundation.AVCaptureFileOutput
import platform.AVFoundation.AVCaptureFileOutputRecordingDelegateProtocol
import platform.Foundation.NSError
import platform.Foundation.NSFileManager
import platform.Foundation.NSURL
import platform.darwin.NSObject

class CameraCaptureFileOutputRecordingDelegateIOS : NSObject(),
    AVCaptureFileOutputRecordingDelegateProtocol {
    @OptIn(ExperimentalForeignApi::class)
    override fun captureOutput(
        output: AVCaptureFileOutput,
        didFinishRecordingToOutputFileAtURL: NSURL,
        fromConnections: List<*>,
        error: NSError?
    ) {
        println(
            "Video size: ${
                NSFileManager.defaultManager.contentsAtPath(
                    didFinishRecordingToOutputFileAtURL.path!!
                )!!.length
            }"
        )
        println("error: $error")
        println(
            "Deleted: ${
                NSFileManager.defaultManager.removeItemAtURL(
                    didFinishRecordingToOutputFileAtURL,
                    null
                )
            }"
        )
    }
}
