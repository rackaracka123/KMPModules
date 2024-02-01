package se.alster.kmp.media.camera

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.ImageBitmap
import se.alster.kmp.media.AspectRatio

@Composable
expect fun CameraView(
    modifier: Modifier,
    aspectRatio: AspectRatio,
    onQrCodeScanned: ((String) -> Unit)?,
    photoController: ((onTakePhoto: ((photo: ImageBitmap) -> Unit) -> Unit) -> Unit)?
)
