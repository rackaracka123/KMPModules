package se.alster.kmp.media.player

sealed class Track {
    data class Mp4Vod(val url: String) : Track()
    data class HlsVod(val url: String) : Track()
    data class HlsDashVod(val hls: String, val dash: String) : Track()
}
