package se.alster.kmp.media.player

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import se.alster.kmp.media.AspectRatio

@Composable
expect fun PlayerView(
    modifier: Modifier,
    player: Player,
    aspectRatio: AspectRatio,
    enableMediaControls: Boolean
)
