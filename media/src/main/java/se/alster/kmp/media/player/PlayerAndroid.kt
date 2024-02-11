package se.alster.kmp.media.player

import android.Manifest
import android.content.Context
import androidx.annotation.OptIn
import androidx.annotation.RequiresPermission
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.LifecycleObserver
import androidx.media3.common.C
import androidx.media3.common.util.UnstableApi
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.exoplayer.source.MediaSource
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import se.alster.kmp.media.player.extensions.buildMediaSource
import se.alster.kmp.media.player.extensions.toAndroidRepeatMode
import kotlin.time.Duration
import kotlin.time.Duration.Companion.milliseconds
import kotlin.time.Duration.Companion.seconds

private val PositionFlowResolution = 100.milliseconds

@UnstableApi
class PlayerAndroid(
    private val context: Context,
    coroutineScope: CoroutineScope,
    addLifecyleObserver: (LifecycleObserver) -> Unit,
    private val removeLifecycleObserver: (LifecycleObserver) -> Unit
) : Player {
    internal val exoPlayer: ExoPlayer = ExoPlayer.Builder(context).build()
    private val _positionFlow = MutableStateFlow(0.seconds)
    private var shouldEmitPositionFlow = true
    private var shouldPausePlayerInBackground = true
    private val lifecycleObserver = LifecycleEventObserver { _, event ->
        if (event == Lifecycle.Event.ON_PAUSE && shouldPausePlayerInBackground) {
            exoPlayer.pause()
        }
    }

    override fun release() {
        removeLifecycleObserver(lifecycleObserver)
        shouldEmitPositionFlow = false
        exoPlayer.release()
    }

    init {
        addLifecyleObserver(lifecycleObserver)
        coroutineScope.launch {
            while (shouldEmitPositionFlow) {
                _positionFlow.value = exoPlayer.currentPosition.milliseconds
                delay(PositionFlowResolution)
            }
        }
    }

    override fun prepareTrackForPlayback(track: Track) {
        val mediaSource = track.buildMediaSource(context)
        exoPlayer.setMediaSource(mediaSource)
        exoPlayer.prepare()
    }

    @OptIn(UnstableApi::class) override fun prepareTrackListForPlayback(
        playList: TrackList,
    ) {
        if (exoPlayer.mediaItemCount > 0) {
            exoPlayer.clearMediaItems()
        }
        val mediaItems = createMediaSources(playList)
        exoPlayer.setMediaSources(mediaItems)
        exoPlayer.prepare()
    }

    @RequiresPermission(Manifest.permission.WAKE_LOCK)
    override fun setAllowBackgroundPlayback(allowBackgroundPlayback: Boolean) {
        shouldPausePlayerInBackground = !allowBackgroundPlayback
        exoPlayer.setWakeMode(
            if (allowBackgroundPlayback) C.WAKE_MODE_NETWORK
            else C.WAKE_MODE_NONE
        )
    }

    override fun addTrackToTrackList(track: Track) {
        val mediaSource = track.buildMediaSource(context)
        exoPlayer.addMediaSource(mediaSource)
    }

    override fun addTrackToTrackList(track: Track, index: Int) {
        val mediaSource = track.buildMediaSource(context)
        exoPlayer.addMediaSource(index, mediaSource)
    }

    override fun removeTrackFromTrackList(index: Int) {
        exoPlayer.removeMediaItem(index)
    }

    override fun replaceAllTracksInTrackList(trackList: TrackList) {
        val mediaItems = createMediaSources(trackList)
        exoPlayer.setMediaSources(mediaItems)
    }

    override fun getCurrentlyPlayingIndex(): Int {
        return exoPlayer.currentMediaItemIndex
    }

    override fun setPlayOnReady(playOnReady: Boolean) {
        exoPlayer.playWhenReady = playOnReady
    }

    override fun setRepeatMode(repeatMode: RepeatMode) {
        exoPlayer.repeatMode = repeatMode.toAndroidRepeatMode()
    }

    override fun position(): Duration {
        return exoPlayer.currentPosition.milliseconds
    }

    override val positionFlow = _positionFlow.asStateFlow()

    override fun duration(): Duration {
        return exoPlayer.duration.milliseconds
    }

    private fun createMediaSources(playlist: TrackList): List<MediaSource> =
        playlist.tracks.map { it.buildMediaSource(context) }

    override fun isPlaybackReady(): Boolean {
        return exoPlayer.isCommandAvailable(ExoPlayer.COMMAND_PLAY_PAUSE)
    }

    override fun play() {
        exoPlayer.play()
    }

    override fun pause() {
        exoPlayer.pause()
    }

    override fun setVolume(volume: Float) {
        exoPlayer.volume = volume
    }

    override fun seekTo(positionMs: Long) {
        exoPlayer.seekTo(positionMs)
    }

    override fun hasNext(): Boolean {
        return exoPlayer.hasNextMediaItem()
    }

    override fun next() {
        exoPlayer.seekToNext()
    }

    override fun hasPrevious(): Boolean {
        return exoPlayer.hasPreviousMediaItem()
    }

    override fun previous() {
        exoPlayer.seekToPrevious()
    }

    override fun isLiveStream(): Boolean = exoPlayer.isCurrentMediaItemLive

    override fun seekToLive() {
        exoPlayer.seekToDefaultPosition()
    }
}
