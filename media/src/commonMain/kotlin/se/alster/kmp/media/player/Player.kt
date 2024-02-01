package se.alster.kmp.media.player

import kotlinx.coroutines.flow.StateFlow
import kotlin.time.Duration

interface Player {
    fun release()
    fun prepareTrackForPlayback(track: Track)
    fun prepareTrackListForPlayback(
        playList: TrackList
    )
    fun setAllowBackgroundPlayback(allowBackgroundPlayback: Boolean)
    fun addTrackToTrackList(track: Track)
    fun addTrackToTrackList(track: Track, index: Int)
    fun removeTrackFromTrackList(index: Int)
    fun replaceTrackInTrackList(track: Track, index: Int)
    fun replaceAllTracksInTrackList(trackList: TrackList)
    fun getCurrentlyPlayingIndex(): Int
    fun setPlayOnReady(playOnReady: Boolean)
    fun setRepeatMode(repeatMode: RepeatMode)
    fun position(): Duration
    val positionFlow: StateFlow<Duration>
    fun play()
    fun pause()
    fun setVolume(volume: Float)
    fun isPlaybackReady(): Boolean
    fun seekTo(positionMs: Long)
    fun hasNext(): Boolean
    fun next()
    fun hasPrevious(): Boolean
    fun previous()
    fun isLiveStream(): Boolean
    fun seekToLive()
    fun duration(): Duration
}
