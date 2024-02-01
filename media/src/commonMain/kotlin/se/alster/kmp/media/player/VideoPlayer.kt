package se.alster.kmp.media.player

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier

@Composable
expect fun PlayerView(modifier: Modifier, player: Player, aspectRatio: AspectRatio)
