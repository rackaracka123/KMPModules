package se.alster.kmp.media.player

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember

@Composable
actual fun provideAlsterPlayer(): Player = remember { PlayerIOS() }
