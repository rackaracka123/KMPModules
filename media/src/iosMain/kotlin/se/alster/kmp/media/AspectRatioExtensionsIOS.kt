package se.alster.kmp.media

import platform.AVFoundation.AVLayerVideoGravityResize
import platform.AVFoundation.AVLayerVideoGravityResizeAspect
import platform.AVFoundation.AVLayerVideoGravityResizeAspectFill

fun AspectRatio.toAVLayerVideoGravity() = when (this) {
    AspectRatio.FitWithAspectRatio -> AVLayerVideoGravityResizeAspect
    AspectRatio.ScaleToFit -> AVLayerVideoGravityResizeAspectFill
    AspectRatio.ScaleToFitWithCropping -> AVLayerVideoGravityResize
}
