package se.alster.kmp.media.camera

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import se.alster.kmp.media.AspectRatio

@Composable
expect fun CameraView(
    modifier: Modifier,
    aspectRatio: AspectRatio,
    onQrCodeScanned: ((String) -> Unit)?,
    captureController: CaptureController?,
    cameraFacing: CameraFacing
)
