package com.nearchat.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable

private val DarkPalette = darkColorScheme(
    primary = AccentBlue,
    secondary = AccentPurple,
    background = BackgroundDark,
    surface = SurfaceDark,
)

@Composable
fun NearChatTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = DarkPalette,
        typography = NearChatTypography,
        content = content,
    )
}
