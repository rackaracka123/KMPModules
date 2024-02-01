package se.alster.kmp.media.player

import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.ui.Modifier
import androidx.compose.ui.interop.UIKitView
import kotlinx.cinterop.ExperimentalForeignApi

@OptIn(ExperimentalForeignApi::class)
@Composable
actual fun PlayerView(modifier: Modifier, player: Player, aspectRatio: AspectRatio) {
    val playerIOS = player as? PlayerIOS ?: return
    playerIOS.setAspectRatio(aspectRatio)

    DisposableEffect(Unit) {
        onDispose {
            playerIOS.release()
        }
    }

    UIKitView(factory = {
        playerIOS.playerViewController.view
    }, modifier = modifier)
}
