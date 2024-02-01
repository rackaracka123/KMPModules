package org.company.app

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.ui.Modifier
import se.alster.kmp.media.AspectRatio
import se.alster.kmp.media.player.PlayerView
import se.alster.kmp.media.player.Track
import se.alster.kmp.media.player.rememberPlayer

@Composable
fun VideoPlayerExample() {
    val player = rememberPlayer()

    PlayerView(
        modifier = Modifier.fillMaxSize(),
        player = player,
        aspectRatio = AspectRatio.FitWithAspectRatio,
        enableMediaControls = true
    )
    DisposableEffect(Unit) {
        player.prepareTrackForPlayback(
            Track.Hls("https://live-par-2-cdn-alt.livepush.io/live/bigbuckbunnyclip/index.m3u8")
        )
        player.setPlayOnReady(true)
        onDispose {
            player.release()
        }
    }
}
