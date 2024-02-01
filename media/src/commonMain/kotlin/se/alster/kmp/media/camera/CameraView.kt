package se.alster.kmp.media.camera

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.ImageBitmap

@Composable
expect fun CameraView(
    modifier: Modifier,
    onQrCodeScanned: ((String) -> Unit)?,
    photoController: ((photoCallback: (photo: (ImageBitmap) -> Unit) -> Unit) -> Unit)?
)
