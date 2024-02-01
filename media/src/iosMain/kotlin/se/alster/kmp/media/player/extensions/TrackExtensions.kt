package se.alster.kmp.media.player.extensions

import platform.AVFoundation.AVPlayerItem
import platform.Foundation.NSURL
import se.alster.kmp.media.player.Track
import se.alster.kmp.media.player.TrackList

fun Track.getUrl() = when (this) {
    is Track.Mp4Vod -> this.url
    is Track.HlsVod -> this.url
    is Track.HlsDashVod -> this.hls
}

fun Track.toAVPlayerItem() = AVPlayerItem(NSURL.URLWithString(getUrl())!!)

fun TrackList.toAVPlayerItems() = tracks.map { it.toAVPlayerItem() }