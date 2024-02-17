package se.alster.kmp.media.player

import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.ui.Modifier
import androidx.compose.ui.interop.UIKitView
import kotlinx.cinterop.ExperimentalForeignApi
import se.alster.kmp.media.AspectRatio

@OptIn(ExperimentalForeignApi::class)
@Composable
actual fun PlayerView(
    modifier: Modifier,
    player: Player,
    aspectRatio: AspectRatio,
    enableMediaControls: Boolean,
    releasePlayerOnDispose: Boolean
) {
    val playerIOS = player as? PlayerIOS ?: return
    playerIOS.setAspectRatio(aspectRatio)
    playerIOS.setEnableMediaControls(enableMediaControls)

    DisposableEffect(playerIOS) {
        onDispose {
            if (releasePlayerOnDispose){
                playerIOS.release()
            }
        }
    }

    UIKitView(factory = {
        playerIOS.playerViewController.view
    }, modifier = modifier)
}
