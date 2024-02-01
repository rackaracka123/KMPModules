package se.alster.kmp.media.player

import androidx.annotation.OptIn
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.ui.Modifier
import androidx.compose.ui.viewinterop.AndroidView
import androidx.media3.common.util.UnstableApi
import se.alster.kmp.media.AspectRatio
import se.alster.kmp.media.player.extensions.toResizeMode
import androidx.media3.ui.PlayerView as AndroidPlayerView

@OptIn(UnstableApi::class)
@Composable
actual fun PlayerView(
    modifier: Modifier,
    player: Player,
    aspectRatio: AspectRatio,
    enableMediaControls: Boolean
) {
    val androidPlayer = player as? PlayerAndroid ?: return

    DisposableEffect(Unit) {
        onDispose {
            player.release()
        }
    }

    AndroidView(factory = {
        AndroidPlayerView(it).apply {
            setPlayer(androidPlayer.exoPlayer)
            resizeMode = aspectRatio.toResizeMode()
            useController = enableMediaControls
        }
    }, modifier = modifier)
}
