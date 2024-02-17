package org.company.app

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Modifier
import kotlinx.coroutines.delay
import se.alster.kmp.media.AspectRatio
import se.alster.kmp.media.player.PlayerView
import se.alster.kmp.media.player.Track
import se.alster.kmp.media.player.rememberPlayer

@Composable
fun VideoPlayerExample() {
    val player1 = rememberPlayer()

    Column {
        PlayerView(
            modifier = Modifier.fillMaxSize(0.5f),
            player = player1,
            aspectRatio = AspectRatio.FitWithAspectRatio,
            enableMediaControls = true,
            releasePlayerOnDispose = true
        )
        PlayerView(
            modifier = Modifier.fillMaxSize(0.5f),
            player = player1,
            aspectRatio = AspectRatio.FitWithAspectRatio,
            enableMediaControls = true,
            releasePlayerOnDispose = true
        )
    }
    LaunchedEffect(Unit) {
        player1.prepareTrackForPlayback(
            Track.Network("https://hls.rackaracka.net/output/lekstugan/lekstugan.m3u8")
        )
        player1.setPlayOnReady(true)
        delay(2000)
    }
}
