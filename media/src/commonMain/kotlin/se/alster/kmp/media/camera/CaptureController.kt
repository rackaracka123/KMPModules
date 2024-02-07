package se.alster.kmp.media.camera

interface CaptureController {
    fun takePicture(callback: (photo: CaptureResult) -> Unit)
    fun startRecording()
    fun stopRecording()
}
