package se.alster.kmp.media.player

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner

@Composable
actual fun provideAlsterPlayer(): Player {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    val coroutineScope = rememberCoroutineScope()
    return remember {
        PlayerAndroid(
            context = context,
            coroutineScope = coroutineScope,
            addLifecyleObserver = {
                lifecycleOwner.lifecycle.addObserver(it)
            },
            removeLifecycleObserver = {
                lifecycleOwner.lifecycle.removeObserver(it)
            })
    }
}
