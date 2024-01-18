package se.alster.kmp.theme

import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.interop.LocalUIViewController
import kotlinx.cinterop.BetaInteropApi
import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.cinterop.ExportObjCClass
import kotlinx.cinterop.readValue
import platform.CoreGraphics.CGRectZero
import platform.UIKit.UITraitCollection
import platform.UIKit.UIUserInterfaceStyle
import platform.UIKit.UIView
import platform.UIKit.UIViewController
import platform.UIKit.currentTraitCollection

@Composable
actual fun isSystemInDarkTheme(): Boolean {
    var style: UIUserInterfaceStyle by remember {
        mutableStateOf(UITraitCollection.currentTraitCollection.userInterfaceStyle)
    }

    val viewController: UIViewController = LocalUIViewController.current
    DisposableEffect(Unit) {
        val view: UIView = viewController.view
        val traitView = TraitView {
            style = UITraitCollection.currentTraitCollection.userInterfaceStyle
        }
        view.addSubview(traitView)

        onDispose {
            traitView.removeFromSuperview()
        }
    }

    return style == UIUserInterfaceStyle.UIUserInterfaceStyleDark
}

@OptIn(ExperimentalForeignApi::class, BetaInteropApi::class)
@ExportObjCClass
private class TraitView(
    private val onTraitChanged: () -> Unit
) : UIView(frame = CGRectZero.readValue()) {
    override fun traitCollectionDidChange(previousTraitCollection: UITraitCollection?) {
        super.traitCollectionDidChange(previousTraitCollection)
        onTraitChanged()
    }
}
