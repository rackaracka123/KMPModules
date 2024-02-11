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
import se.alster.kmp.storage.FilePath
import java.io.File

@OptIn(UnstableApi::class)
fun Track.buildMediaSource(context: Context): MediaSource =
    when (this) {
        is Track.File -> ProgressiveMediaSource
            .Factory(FileDataSource.Factory())
            .createMediaSource(
                MediaItem.fromUri(filePath.toFile(context.filesDir).toUri())
            )

        is Track.Network -> DefaultMediaSourceFactory(context).createMediaSource(
            MediaItem.Builder()
                .setUri(url)
                .build()
        )
    }


private fun FilePath.toFile(filesDir: File): File =
    File(filesDir, path)

