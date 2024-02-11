package org.company.app

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.ui.Modifier
import se.alster.kmp.media.AspectRatio
import se.alster.kmp.media.player.PlayerView
import se.alster.kmp.media.player.Track
import se.alster.kmp.media.player.TrackList
import se.alster.kmp.media.player.TrackLocation
import se.alster.kmp.media.player.rememberPlayer
import se.alster.kmp.storage.FilePath
import se.alster.kmp.storage.Location

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
        // Track.Hls(Uri("https://live-par-2-cdn-alt.livepush.io/live/bigbuckbunnyclip/index.m3u8"))
        player.prepareTrackListForPlayback(
            TrackList(
                listOf(
                    Track.Mp4(TrackLocation.File(FilePath("video.mp4", Location.Documents))),
                    Track.Mp4(TrackLocation.File(FilePath("video.mp4", Location.Documents))),
                )
            )
        )
        player.setPlayOnReady(true)
        onDispose {
            player.release()
        }
    }
}
