package se.alster.kmp.media.player.extensions

import kotlinx.cinterop.CValue
import kotlinx.cinterop.ExperimentalForeignApi
import platform.CoreMedia.CMTime
import platform.CoreMedia.CMTimeGetSeconds
import platform.darwin.Float64
import kotlin.time.Duration
import kotlin.time.Duration.Companion.seconds

fun Float64.seconds(): Duration = if (this.isNaN()) {
    0.seconds
} else if (this.isInfinite()) {
    Duration.INFINITE
} else {
    this.seconds
}

@OptIn(ExperimentalForeignApi::class)
fun CValue<CMTime>.toDuration() = CMTimeGetSeconds(this).seconds()
