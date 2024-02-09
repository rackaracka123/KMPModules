package se.alster.kmp.media.camera

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember

@Composable
actual fun rememberCaptureController(): CaptureController = remember { Foo() }
