package se.alster.kmp.media.camera


import kotlinx.cinterop.ExperimentalForeignApi
import platform.AVFoundation.AVCaptureFileOutput
import platform.AVFoundation.AVCaptureFileOutputRecordingDelegateProtocol
import platform.Foundation.NSError
import platform.Foundation.NSFileManager
import platform.Foundation.NSURL
import platform.darwin.NSObject
import se.alster.kmp.storage.FilePath
import se.alster.kmp.storage.Location
import se.alster.kmp.storage.StorageIOS
import se.alster.kmp.storage.toFilePath

class CameraCaptureFileOutputRecordingDelegateIOS : NSObject(),
    AVCaptureFileOutputRecordingDelegateProtocol {
    override fun captureOutput(
        output: AVCaptureFileOutput,
        didFinishRecordingToOutputFileAtURL: NSURL,
        fromConnections: List<*>,
        error: NSError?
    ) {
        val storage = StorageIOS()
        val file = didFinishRecordingToOutputFileAtURL.toFilePath()
        val video = storage.read(file)
        println("Video size: ${video.size}")
        println("error: $error")
        println("Deleted: ${storage.delete(file)}")
    }
}
