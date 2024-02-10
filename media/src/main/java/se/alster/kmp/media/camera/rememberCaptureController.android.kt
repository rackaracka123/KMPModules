package se.alster.kmp.media.camera

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext

@Composable
actual fun rememberCaptureController(): CaptureController {
    val context = LocalContext.current
    return remember { CaptureControllerAndroid(context) }
}
