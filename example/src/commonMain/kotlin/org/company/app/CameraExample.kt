package org.company.app

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import se.alster.kmp.media.AspectRatio
import se.alster.kmp.media.camera.CameraView

@Composable
fun CameraExample(){
    val coroutineScope = rememberCoroutineScope()
    CameraView(
        modifier = Modifier.fillMaxSize(),
        aspectRatio = AspectRatio.FitWithAspectRatio,
        onQrCodeScanned = {
            println("QR code scanned: $it")
        },
        photoController = {
            coroutineScope.launch {
                delay(2000)
                it {
                    println("Photo taken")
                }
            }
        }
    )
}
