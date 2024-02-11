package se.alster.kmp.media.player.extensions

import platform.AVFoundation.AVPlayerItem
import platform.Foundation.NSURL
import se.alster.kmp.media.player.Track
import se.alster.kmp.media.player.TrackList
import se.alster.kmp.storage.toNSURL

fun Track.toAVPlayerItem() =
    when (this) {
        is Track.File -> AVPlayerItem(filePath.toNSURL())
        is Track.Network -> AVPlayerItem(NSURL.URLWithString(this.url)!!)
    }

fun TrackList.toAVPlayerItems() = tracks.map { it.toAVPlayerItem() }
