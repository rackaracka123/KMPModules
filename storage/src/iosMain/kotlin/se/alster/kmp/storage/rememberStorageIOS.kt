package se.alster.kmp.storage

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember

@Composable
actual fun rememberStorage(): Storage = remember { StorageIOS() }
