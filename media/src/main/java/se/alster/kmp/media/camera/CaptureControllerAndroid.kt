package se.alster.kmp.media.camera

import android.content.Context
import androidx.annotation.OptIn
import androidx.camera.core.ExperimentalGetImage
import androidx.camera.core.ImageCapture
import androidx.camera.core.ImageCaptureException
import androidx.camera.core.ImageProxy
import androidx.camera.video.ExperimentalPersistentRecording
import androidx.camera.video.FallbackStrategy
import androidx.camera.video.FileOutputOptions
import androidx.camera.video.Quality
import androidx.camera.video.QualitySelector
import androidx.camera.video.Recorder
import androidx.camera.video.Recording
import androidx.camera.video.VideoCapture
import androidx.camera.video.VideoRecordEvent
import androidx.core.content.ContextCompat
import se.alster.kmp.media.extensions.moveToByteArray
import se.alster.kmp.media.extensions.toImageBitmap
import se.alster.kmp.storage.FilePath
import se.alster.kmp.storage.StorageAndroid
import java.io.File

class CaptureControllerAndroid(private val context: Context) : CaptureController {

    internal val imageCapture = ImageCapture.Builder().build()

    internal val executor = ContextCompat.getMainExecutor(context)
    private val qualitySelector = QualitySelector.fromOrderedList(
        listOf(Quality.UHD, Quality.FHD, Quality.HD, Quality.SD),
        FallbackStrategy.lowerQualityOrHigherThan(Quality.SD)
    )
    private val recorder = Recorder.Builder()
        .setQualitySelector(qualitySelector)
        .setExecutor(executor)
        .build()
    internal val videoCapture = VideoCapture.withOutput(recorder)
    private var recording: Recording? = null

    override fun takePicture(callback: (photo: ImageCaptureResult) -> Unit) {
        imageCapture.takePicture(
            executor,
            object :
                ImageCapture.OnImageCapturedCallback() {
                @OptIn(ExperimentalGetImage::class)
                override fun onCaptureSuccess(image: ImageProxy) {
                    super.onCaptureSuccess(image)
                    image.use { imageProxy ->
                        callback(
                            imageProxy.image?.planes?.getOrNull(0)?.buffer
                                ?.moveToByteArray()
                                ?.toImageBitmap()
                                ?.let { ImageCaptureResult.Success(it) }
                                ?: ImageCaptureResult.Failure
                        )
                    }
                }

                override fun onError(exception: ImageCaptureException) {
                    super.onError(exception)
                    callback(ImageCaptureResult.Failure)
                }
            }
        )
    }

    @OptIn(ExperimentalPersistentRecording::class)
    override fun startRecording(filepath: FilePath) {
        val pendingRecording = recorder.prepareRecording(
            context, FileOutputOptions
                .Builder(File(context.filesDir, filepath.path))
                .build()
        )
        recording = pendingRecording
            .start(executor) {
                // TODO: Provide a way to listen to the recording events
                when (it) {
                    is VideoRecordEvent.Finalize -> {
                        StorageAndroid(context.filesDir).apply {
                            read(filepath).let { file ->
                                println("File size: ${file.size}")
                            }
                            delete(filepath)
                        }
                    }
                }
            }
    }

    override fun stopRecording() {
        recording?.stop()
    }
}
