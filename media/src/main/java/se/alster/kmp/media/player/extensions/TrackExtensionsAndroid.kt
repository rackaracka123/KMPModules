package se.alster.kmp.media.player.extensions

import android.content.Context
import androidx.annotation.OptIn
import androidx.core.net.toUri
import androidx.media3.common.MediaItem
import androidx.media3.common.util.UnstableApi
import androidx.media3.datasource.FileDataSource
import androidx.media3.exoplayer.source.DefaultMediaSourceFactory
import androidx.media3.exoplayer.source.MediaSource
import androidx.media3.exoplayer.source.ProgressiveMediaSource
import se.alster.kmp.media.player.Track
import se.alster.kmp.media.player.TrackLocation
import se.alster.kmp.storage.FilePath
import java.io.File

@OptIn(UnstableApi::class)
fun Track.buildMediaSource(context: Context): MediaSource =
    when (this) {
        is Track.Mp4 -> location.createMediaSource(context)
        is Track.Hls -> location.createMediaSource(context)
        is Track.HlsDash -> hlsLocation.createMediaSource(context)
    }

@UnstableApi
private fun TrackLocation.createMediaSource(context: Context) =
    when (this) {
        is TrackLocation.File -> ProgressiveMediaSource
            .Factory(FileDataSource.Factory())
            .createMediaSource(
                MediaItem.fromUri(filePath.toFile(context.filesDir).toUri())
            )

        is TrackLocation.Network -> DefaultMediaSourceFactory(context).createMediaSource(
            MediaItem.Builder()
                .setUri(url)
                .build()
        )
    }

private fun FilePath.toFile(filesDir: File): File =
    File(filesDir, path)

