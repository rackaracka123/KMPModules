package se.alster.kmp.storage

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext

@Composable
actual fun rememberStorage(): Storage {
    val context = LocalContext.current
    return remember { StorageAndroid(context.filesDir) }
}
