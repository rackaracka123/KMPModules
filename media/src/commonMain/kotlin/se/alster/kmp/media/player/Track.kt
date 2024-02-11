package se.alster.kmp.media.player

import se.alster.kmp.storage.FilePath

sealed class TrackLocation {
    data class File(val filePath: FilePath) : TrackLocation()
    data class Network(val url: String) : TrackLocation()
}

sealed class Track {
    data class Mp4(val location: TrackLocation) : Track()
    data class Hls(val location: TrackLocation) : Track()
    data class HlsDash(val hlsLocation: TrackLocation, val dashLocation: TrackLocation) : Track()
}
