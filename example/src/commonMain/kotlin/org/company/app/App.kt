package org.company.app

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import se.alster.kmp.media.camera.CameraView

@Composable
internal fun App() {
    CameraView(modifier = Modifier.fillMaxSize())
}
