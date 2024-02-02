package org.company.app

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
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

@Composable
fun CameraExample(){
    val coroutineScope = rememberCoroutineScope()
    var cameraFacing by remember { mutableStateOf(CameraFacing.Back) }
    coroutineScope.launch {
        delay(5000)
        cameraFacing = CameraFacing.Front
    }
    CameraView(
        modifier = Modifier.fillMaxSize(),
        aspectRatio = AspectRatio.FitWithAspectRatio,
        onQrCodeScanned = {
            println("QR code scanned: $it")
        },
        takePhotoController = { onTakePhoto ->
            coroutineScope.launch {
                delay(1000)
                onTakePhoto { photo ->
                    println("Photo taken: $photo")
                }
            }
        },
        cameraFacing = cameraFacing
    )
}
