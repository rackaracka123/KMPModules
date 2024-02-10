package org.company.app

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import se.alster.kmp.media.AspectRatio
import se.alster.kmp.media.camera.CameraFacing
import se.alster.kmp.media.camera.CameraView
import se.alster.kmp.media.camera.CaptureController
import se.alster.kmp.media.camera.rememberCaptureController
import se.alster.kmp.storage.FilePath
import se.alster.kmp.storage.Location

@Composable
fun CameraExample() {
    var cameraFacing by remember { mutableStateOf(CameraFacing.Back) }
    val cameraController = rememberCaptureController()
    LaunchedEffect(Unit) {
        delay(1000)
        cameraController.startRecording(FilePath("video.mp4", Location.Documents))
        delay(3000)
        cameraController.stopRecording()
    }
    CameraView(
        modifier = Modifier.fillMaxSize(),
        aspectRatio = AspectRatio.FitWithAspectRatio,
        onQrCodeScanned = {
            println("QR code scanned: $it")
        },
        captureController = cameraController,
        cameraFacing = cameraFacing
    )
}
