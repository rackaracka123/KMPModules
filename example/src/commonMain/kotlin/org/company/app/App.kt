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
internal fun App() {
    val player = rememberPlayer()

    PlayerView(
        modifier = Modifier.fillMaxSize(),
        player = player,
        aspectRatio = AspectRatio.FitWithAspectRatio,
        enableMediaControls = true
    )
    DisposableEffect(Unit) {
        player.prepareTrackForPlayback(
            Track.Mp4Vod("https://commondatastorage.googleapis.com/gtv-videos-bucket/sample/ElephantsDream.mp4")
        )
        player.addTrackToTrackList(Track.Mp4Vod("https://commondatastorage.googleapis.com/gtv-videos-bucket/sample/ForBiggerEscapes.mp4"))
        onDispose {
            player.release()
        }
    }
}
