package se.alster.kmp.media.player.extensions

import androidx.media3.common.Player
import se.alster.kmp.media.player.RepeatMode

internal fun RepeatMode.toAndroidRepeatMode(): Int = when (this) {
    RepeatMode.OFF -> Player.REPEAT_MODE_OFF
    RepeatMode.ONE -> Player.REPEAT_MODE_ONE
    RepeatMode.ALL -> Player.REPEAT_MODE_ALL
}
