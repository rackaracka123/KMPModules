package se.alster.kmp.media.player.extensions

import platform.AVFoundation.AVLayerVideoGravityResize
import platform.AVFoundation.AVLayerVideoGravityResizeAspect
import platform.AVFoundation.AVLayerVideoGravityResizeAspectFill
import se.alster.kmp.media.player.AspectRatio

fun AspectRatio.toAVLayerVideoGravity() = when (this) {
    AspectRatio.FitWithAspectRatio -> AVLayerVideoGravityResizeAspect
    AspectRatio.ScaleToFit -> AVLayerVideoGravityResizeAspectFill
    AspectRatio.ScaleToFitWithCropping -> AVLayerVideoGravityResize
}
