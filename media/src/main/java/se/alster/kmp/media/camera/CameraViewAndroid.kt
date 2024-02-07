package se.alster.kmp.media.camera

import android.Manifest
import android.content.Context
import android.util.Size
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.camera.core.CameraSelector
import androidx.camera.core.ExperimentalGetImage
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.ImageCapture
import androidx.camera.core.ImageCaptureException
import androidx.camera.core.ImageProxy
import androidx.camera.core.Preview
import androidx.camera.core.UseCaseGroup
import androidx.camera.core.resolutionselector.ResolutionSelector
import androidx.camera.core.resolutionselector.ResolutionStrategy
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.video.FallbackStrategy
import androidx.camera.video.FileOutputOptions
import androidx.camera.video.Quality
import androidx.camera.video.QualitySelector
import androidx.camera.video.Recorder
import androidx.camera.video.Recording
import androidx.camera.video.VideoCapture
import androidx.camera.view.PreviewView
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import androidx.lifecycle.LifecycleOwner
import se.alster.kmp.media.AspectRatio
import se.alster.kmp.media.extensions.moveToByteArray
import se.alster.kmp.media.extensions.toImageBitmap
import se.alster.kmp.media.extensions.toLensFacing
import java.io.File
import java.util.concurrent.Executor

@Composable
actual fun CameraView(
    modifier: Modifier,
    aspectRatio: AspectRatio,
    onQrCodeScanned: ((String) -> Unit)?,
    captureController: (CaptureController.() -> Unit)?,
    cameraFacing: CameraFacing
) {
    val context = LocalContext.current
    val imageCapture = remember(context) { ImageCapture.Builder().build() }
    val executor = remember(context) { ContextCompat.getMainExecutor(context) }
    val qualitySelector = QualitySelector.fromOrderedList(
        listOf(Quality.UHD, Quality.FHD, Quality.HD, Quality.SD),
        FallbackStrategy.lowerQualityOrHigherThan(Quality.SD)
    )
    val recorder = remember(context) {
        Recorder.Builder()
            .setQualitySelector(qualitySelector)
            .setExecutor(executor)
            .build()
    }
    val videoCapture = remember(context) {
        VideoCapture.withOutput(recorder)
    }
    var recording: Recording? by remember { mutableStateOf(null) }

    captureController?.invoke(object : CaptureController {
        override fun takePicture(callback: (photo: CaptureResult) -> Unit) {
            imageCapture.takePicture(
                executor,
                object :
                    ImageCapture.OnImageCapturedCallback() {
                    @androidx.annotation.OptIn(ExperimentalGetImage::class)
                    override fun onCaptureSuccess(image: ImageProxy) {
                        super.onCaptureSuccess(image)
                        image.use { imageProxy ->
                            callback(
                                imageProxy.image?.planes?.getOrNull(0)?.buffer
                                    ?.moveToByteArray()
                                    ?.toImageBitmap()
                                    ?.let { CaptureResult.Success(it) }
                                    ?: CaptureResult.Failure
                            )
                        }
                    }

                    override fun onError(exception: ImageCaptureException) {
                        super.onError(exception)
                        callback(CaptureResult.Failure)
                    }
                }
            )
        }

        override fun startRecording() {
            println("Start recording")
            recording = recorder.prepareRecording(
                context, FileOutputOptions
                    .Builder(File(context.filesDir, "video.mp4"))
                    .build()
            ).start(executor
            ) {}
        }

        override fun stopRecording() {
            println("Stop recording")
            recording?.stop()
        }
    })

    CameraViewAndroid(
        modifier = modifier,
        aspectRatio = aspectRatio,
        executor = executor,
        imageCapture = imageCapture,
        cameraFacing = cameraFacing,
        videoCapture = videoCapture
    )
}

@Composable
private fun CameraViewAndroid(
    modifier: Modifier = Modifier,
    aspectRatio: AspectRatio,
    context: Context = LocalContext.current,
    lifecycleOwner: LifecycleOwner = LocalLifecycleOwner.current,
    executor: Executor = remember { ContextCompat.getMainExecutor(context) },
    imageAnalyzer: ImageAnalysis.Analyzer? = null,
    imageCapture: ImageCapture? = null,
    videoCapture: VideoCapture<Recorder>? = null,
    cameraFacing: CameraFacing = CameraFacing.Back
) {
    val previewCameraView = remember(context) { PreviewView(context) }
    val cameraProviderFuture = remember(context) { ProcessCameraProvider.getInstance(context) }
    var cameraPermission by remember { mutableStateOf(false) }
    val launcher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted: Boolean ->
        cameraPermission = isGranted
    }
    LaunchedEffect(Unit) {
        launcher.launch(Manifest.permission.CAMERA)
    }

    DisposableEffect(cameraProviderFuture, cameraPermission, cameraFacing) {
        val cameraProvider = cameraProviderFuture.get()
        previewCameraView.scaleType = aspectRatio.toScaleType()
        cameraProviderFuture.addListener(
            {
                val cameraSelector = CameraSelector.Builder()
                    .requireLensFacing(cameraFacing.toLensFacing())
                    .build()

                val preview = Preview.Builder().build().apply {
                    setSurfaceProvider(previewCameraView.surfaceProvider)
                }

                val imageAnalysis = ImageAnalysis.Builder()
                    .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
                    .setResolutionSelector(
                        ResolutionSelector.Builder()
                            .setResolutionStrategy(
                                ResolutionStrategy(
                                    Size(1280, 960),
                                    ResolutionStrategy.FALLBACK_RULE_CLOSEST_LOWER_THEN_HIGHER
                                )
                            )
                            .build()
                    )
                    .build()
                    .apply { imageAnalyzer?.let { setAnalyzer(executor, it) } }

                cameraProvider.unbindAll()
                val useCaseGroupBuilder = UseCaseGroup.Builder()

                listOfNotNull(
                    imageAnalysis,
                    imageCapture,
                    videoCapture,
                    preview
                ).forEach { useCaseGroupBuilder.addUseCase(it) }

                cameraProvider.bindToLifecycle(
                    lifecycleOwner,
                    cameraSelector,
                    useCaseGroupBuilder.build()
                )
            },
            executor
        )

        onDispose {
            cameraProvider.unbindAll()
        }
    }

    AndroidView(
        modifier = modifier,
        factory = {
            previewCameraView
        }
    )
}
