package se.alster.kmp.media.camera

import se.alster.kmp.storage.FilePath

interface CaptureController {
    fun takePicture(callback: (photo: CaptureResult) -> Unit)
    fun startRecording(filepath: FilePath)
    fun stopRecording()
}
