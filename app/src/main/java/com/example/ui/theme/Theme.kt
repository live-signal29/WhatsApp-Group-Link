package com.example.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable

private val LightColorScheme = lightColorScheme(
    primary = PrimaryGreen,
    onPrimary = White,
    primaryContainer = LightGreenContainer,
    onPrimaryContainer = DarkGreen,
    secondary = SecondaryGray,
    onSecondary = White,
    secondaryContainer = LightGray,
    onSecondaryContainer = DarkText,
    tertiary = PromotedYellow,
    onTertiary = DarkText,
    background = White,
    onBackground = DarkText,
    surface = White,
    onSurface = DarkText,
    surfaceVariant = LightGray,
    onSurfaceVariant = SecondaryGray,
    outline = BorderGray
)

@Composable
fun GroupLinksTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    // Group Links uses light mode by default as per design specification
    MaterialTheme(
        colorScheme = LightColorScheme,
        typography = Typography,
        content = content
    )
}

@Composable
fun MyApplicationTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = false,
    content: @Composable () -> Unit
) {
    GroupLinksTheme(darkTheme = darkTheme, content = content)
}
