package se.alster.kmp.media.camera

import androidx.camera.view.PreviewView
import se.alster.kmp.media.AspectRatio

internal fun AspectRatio.toScaleType(): PreviewView.ScaleType = when (this) {
    AspectRatio.FitWithAspectRatio -> PreviewView.ScaleType.FIT_CENTER
    AspectRatio.ScaleToFitWithCropping -> PreviewView.ScaleType.FILL_CENTER
    AspectRatio.ScaleToFit -> PreviewView.ScaleType.FIT_CENTER
}
