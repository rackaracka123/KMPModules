package org.company.app

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import se.alster.kmp.media.AspectRatio
import se.alster.kmp.media.camera.CameraView
import se.alster.kmp.media.player.PlayerView
import se.alster.kmp.media.player.Track
import se.alster.kmp.media.player.rememberPlayer

@Composable
internal fun App() {
    CameraExample()
}
