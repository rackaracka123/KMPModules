package se.alster.kmp.media.player

import se.alster.kmp.storage.FilePath

sealed class Track {
    data class File(val filePath: FilePath) : Track()
    data class Network(val url: String) : Track()
}
