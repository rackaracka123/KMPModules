package se.alster.kmp.media.player

sealed class Track {
    data class Mp4(val url: String) : Track()
    data class Hls(val url: String) : Track()
    data class HlsDash(val hls: String, val dash: String) : Track()
}
