package se.alster.kmp.media.camera

import android.Manifest
import android.content.Context
import android.util.Size
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.ImageCapture
import androidx.camera.core.Preview
import androidx.camera.core.UseCaseGroup
import androidx.camera.core.resolutionselector.ResolutionSelector
import androidx.camera.core.resolutionselector.ResolutionStrategy
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import androidx.lifecycle.LifecycleOwner
import se.alster.kmp.media.AspectRatio
import se.alster.kmp.media.player.extensions.toResizeMode
import java.util.concurrent.Executor

@Composable
actual fun CameraView(
    modifier: Modifier,
    aspectRatio: AspectRatio,
    onQrCodeScanned: ((String) -> Unit)?,
    photoController: ((photoCallback: (photo: (ImageBitmap) -> Unit) -> Unit) -> Unit)?
) {
    CameraViewAndroid(modifier, aspectRatio)
}

@Composable
private fun CameraViewAndroid(
    modifier: Modifier = Modifier,
    aspectRatio: AspectRatio,
    context: Context = LocalContext.current,
    lifecycleOwner: LifecycleOwner = LocalLifecycleOwner.current,
    executor: Executor = remember { ContextCompat.getMainExecutor(context) },
    imageAnalyzer: ImageAnalysis.Analyzer? = null,
    imageCapture: ImageCapture? = null
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

    DisposableEffect(cameraProviderFuture, cameraPermission) {
        val cameraProvider = cameraProviderFuture.get()
        previewCameraView.scaleType = aspectRatio.toScaleType()
        cameraProviderFuture.addListener(
            {
                val cameraSelector = CameraSelector.Builder()
                    .requireLensFacing(CameraSelector.LENS_FACING_BACK)
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
