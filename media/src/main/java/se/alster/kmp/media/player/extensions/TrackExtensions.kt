package se.alster.kmp.media.player.extensions

import androidx.media3.common.MediaItem
import se.alster.kmp.media.player.Track

fun Track.buildMediaItem(): MediaItem {
    return MediaItem.Builder()
        .setUri(this.getUrl())
        .build()
}

fun Track.getUrl() = when (this) {
    is Track.Mp4 -> this.url
    is Track.Hls -> this.url
    is Track.HlsDash -> this.dash
}
