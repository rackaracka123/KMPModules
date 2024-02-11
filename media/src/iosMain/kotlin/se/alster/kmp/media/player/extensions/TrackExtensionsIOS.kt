package se.alster.kmp.media.player.extensions

import platform.AVFoundation.AVPlayerItem
import platform.Foundation.NSURL
import se.alster.kmp.media.player.Track
import se.alster.kmp.media.player.TrackList
import se.alster.kmp.media.player.TrackLocation
import se.alster.kmp.storage.toNSURL

fun Track.getTrackLocation() = when (this) {
    is Track.Mp4 -> this.location
    is Track.Hls -> this.location
    is Track.HlsDash -> this.hlsLocation
}

fun Track.toAVPlayerItem() =
    AVPlayerItem(getTrackLocation().toNSURL())

private fun TrackLocation.toNSURL(): NSURL = when (this) {
    is TrackLocation.File -> filePath.toNSURL()
    is TrackLocation.Network -> NSURL.URLWithString(this.url)!!
}

fun TrackList.toAVPlayerItems() = tracks.map { it.toAVPlayerItem() }
