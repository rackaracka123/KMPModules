package se.alster.kmp.theme

import androidx.compose.runtime.Composable

/**
 * Returns true if the current system theme is dark, false otherwise.
 */
@Composable
expect fun isSystemInDarkTheme(): Boolean
