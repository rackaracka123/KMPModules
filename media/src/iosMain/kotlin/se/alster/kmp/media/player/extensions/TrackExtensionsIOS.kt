package se.alster.kmp.media.player.extensions

import platform.AVFoundation.AVPlayerItem
import platform.Foundation.NSURL
import se.alster.kmp.media.player.Track
import se.alster.kmp.media.player.TrackList

fun Track.getUrl() = when (this) {
    is Track.Mp4 -> this.url
    is Track.Hls -> this.url
    is Track.HlsDash -> this.hls
}

fun Track.toAVPlayerItem() = AVPlayerItem(NSURL.URLWithString(getUrl())!!)

fun TrackList.toAVPlayerItems() = tracks.map { it.toAVPlayerItem() }
