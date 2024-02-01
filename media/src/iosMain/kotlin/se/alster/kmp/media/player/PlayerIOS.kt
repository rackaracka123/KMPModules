package se.alster.kmp.media.player

import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import platform.AVFoundation.AVPlayer
import platform.AVFoundation.AVPlayerAudiovisualBackgroundPlaybackPolicyAutomatic
import platform.AVFoundation.AVPlayerAudiovisualBackgroundPlaybackPolicyContinuesIfPossible
import platform.AVFoundation.AVPlayerItem
import platform.AVFoundation.AVPlayerItemDidPlayToEndTimeNotification
import platform.AVFoundation.AVPlayerItemStatusFailed
import platform.AVFoundation.AVPlayerItemStatusReadyToPlay
import platform.AVFoundation.addPeriodicTimeObserverForInterval
import platform.AVFoundation.automaticallyWaitsToMinimizeStalling
import platform.AVFoundation.currentItem
import platform.AVFoundation.currentTime
import platform.AVFoundation.duration
import platform.AVFoundation.pause
import platform.AVFoundation.play
import platform.AVFoundation.playImmediatelyAtRate
import platform.AVFoundation.removeTimeObserver
import platform.AVFoundation.replaceCurrentItemWithPlayerItem
import platform.AVFoundation.seekToTime
import platform.AVFoundation.setAudiovisualBackgroundPlaybackPolicy
import platform.AVFoundation.setExternalPlaybackVideoGravity
import platform.AVFoundation.volume
import platform.AVKit.AVPlayerViewController
import platform.CoreMedia.CMTimeMake
import platform.Foundation.NSNotificationCenter
import platform.Foundation.NSURL.Companion.URLWithString
import se.alster.kmp.media.player.extensions.getUrl
import se.alster.kmp.media.player.extensions.toAVLayerVideoGravity
import se.alster.kmp.media.player.extensions.toAVPlayerItem
import se.alster.kmp.media.player.extensions.toAVPlayerItems
import se.alster.kmp.media.player.extensions.toDuration
import kotlin.time.Duration
import kotlin.time.Duration.Companion.seconds

private const val PeriodicTimeObserverTimeScale = 600

@OptIn(ExperimentalForeignApi::class)
class PlayerIOS : Player {
    private var avPlayer = AVPlayer()
    private val playerItems = mutableListOf<AVPlayerItem>()
    private var repeatMode = RepeatMode.OFF
    private var currentPlayingIndex: Int? = 0
    private val currentPlayingItem
        get() = currentPlayingIndex?.let { playerItems.getOrNull(it) }
    internal val playerViewController = AVPlayerViewController().apply {
        setPlayer(avPlayer)
    }
    private val _positionFlow = MutableStateFlow(0.seconds)
    private val addPeriodicTimeObserverForInterval = avPlayer.addPeriodicTimeObserverForInterval(
        CMTimeMake(1, PeriodicTimeObserverTimeScale),
        null,
    ) { time ->
        _positionFlow.value = time.toDuration()
    }
    private val trackCompletedObserver = NSNotificationCenter.defaultCenter.addObserverForName(
        name = AVPlayerItemDidPlayToEndTimeNotification,
        `object` = avPlayer.currentItem,
        queue = null,
    ) {
        when (repeatMode) {
            RepeatMode.OFF -> next()
            RepeatMode.ONE -> {
                restartCurrentPlayingItem()
                repeatMode = RepeatMode.OFF
            }
            RepeatMode.ALL -> restartCurrentPlayingItem()
        }
    }

    fun setEnableMediaControls(enableMediaControls: Boolean) {
        playerViewController.showsPlaybackControls = enableMediaControls
    }

    override fun release() {
        avPlayer.removeTimeObserver(addPeriodicTimeObserverForInterval)
        NSNotificationCenter.defaultCenter.removeObserver(trackCompletedObserver)
        avPlayer.pause()
        avPlayer.replaceCurrentItemWithPlayerItem(null)
    }

    override fun prepareTrackForPlayback(track: Track) {
        playerItems.clear()
        val playerItem = AVPlayerItem(URLWithString(track.getUrl())!!)
        playerItems.add(playerItem)
        avPlayer.replaceCurrentItemWithPlayerItem(playerItem)
    }

    override fun prepareTrackListForPlayback(playList: TrackList) {
        playerItems.clear()
        playerItems.addAll(playList.tracks.map { AVPlayerItem(URLWithString(it.getUrl())!!) })
        currentPlayingIndex = 0
        avPlayer.replaceCurrentItemWithPlayerItem(currentPlayingItem)
    }

    override fun setAllowBackgroundPlayback(allowBackgroundPlayback: Boolean) {
        avPlayer.setAudiovisualBackgroundPlaybackPolicy(
            if (allowBackgroundPlayback) AVPlayerAudiovisualBackgroundPlaybackPolicyContinuesIfPossible
            else AVPlayerAudiovisualBackgroundPlaybackPolicyAutomatic
        )
    }

    override fun addTrackToTrackList(track: Track) {
        playerItems.add(track.toAVPlayerItem())
    }

    override fun addTrackToTrackList(track: Track, index: Int) {
        playerItems.add(index, track.toAVPlayerItem())
    }

    override fun removeTrackFromTrackList(index: Int) {
        playerItems.removeAt(index)
    }

    override fun replaceTrackInTrackList(track: Track, index: Int) {
        playerItems[index] = track.toAVPlayerItem()
    }

    override fun replaceAllTracksInTrackList(trackList: TrackList) {
        playerItems.clear()
        playerItems.addAll(trackList.toAVPlayerItems())
    }

    override fun getCurrentlyPlayingIndex(): Int {
        return currentPlayingIndex ?: -1
    }

    override fun setPlayOnReady(playOnReady: Boolean) {
        if (playOnReady) {
            avPlayer.automaticallyWaitsToMinimizeStalling = true
            avPlayer.playImmediatelyAtRate(1.0f)
        } else {
            avPlayer.playImmediatelyAtRate(0f)
        }
    }

    override fun setRepeatMode(repeatMode: RepeatMode) {
        this.repeatMode = repeatMode
    }

    @OptIn(ExperimentalForeignApi::class)
    override fun position(): Duration {
        return avPlayer.currentTime().toDuration()
    }

    override val positionFlow = _positionFlow.asStateFlow()
    fun setAspectRatio(aspectRatio: AspectRatio) {
        with(avPlayer) {
            setExternalPlaybackVideoGravity(
                aspectRatio.toAVLayerVideoGravity()
            )
        }
    }

    @OptIn(ExperimentalForeignApi::class)
    override fun duration(): Duration {
        return currentPlayingItem?.duration?.toDuration() ?: 0.seconds
    }

    override fun isPlaybackReady(): Boolean {
        return currentPlayingItem?.status == AVPlayerItemStatusReadyToPlay
    }

    override fun play() {
        when (currentPlayingItem?.status) {
            AVPlayerItemStatusReadyToPlay -> avPlayer.play()
            AVPlayerItemStatusFailed -> println("Failed to play")
            else -> println("Unknown player status")
        }
    }

    override fun pause() {
        avPlayer.pause()
    }

    override fun setVolume(volume: Float) {
        avPlayer.volume = volume
    }

    @OptIn(ExperimentalForeignApi::class)
    override fun seekTo(positionMs: Long) {
        val seekTime = CMTimeMake(positionMs, 1000)
        avPlayer.seekToTime(seekTime)
    }

    override fun hasNext(): Boolean = (playerItems.size - (currentPlayingIndex ?: 0)) > 1

    override fun next() {
        if (hasNext()) {
            currentPlayingIndex = currentPlayingIndex?.plus(1)
            currentPlayingItem?.let {
                avPlayer.replaceCurrentItemWithPlayerItem(it)
            }
        }
    }

    override fun hasPrevious(): Boolean = (currentPlayingIndex ?: 0) > 0

    override fun previous() {
        if (hasPrevious()) {
            currentPlayingIndex = currentPlayingIndex?.minus(1)
            currentPlayingItem?.let {
                avPlayer.replaceCurrentItemWithPlayerItem(it)
            }
        }
    }

    override fun isLiveStream(): Boolean {
        throw NotImplementedError("Not implemented for iOS")
    }

    override fun seekToLive() {
        throw NotImplementedError("Not implemented for iOS")
    }

    private fun restartCurrentPlayingItem() {
        avPlayer.seekToTime(CMTimeMake(0, 1))
        avPlayer.play()
    }
}
