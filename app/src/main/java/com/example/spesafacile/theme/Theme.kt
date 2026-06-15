package com.example.spesafacile.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val LightColorScheme = lightColorScheme(
    primary = OrangePrimary,
    onPrimary = Color.White,
    primaryContainer = OrangeContainer,
    onPrimaryContainer = OrangeOnContainer,
    secondary = OrangeSecondary,
    onSecondary = Color.White,
    secondaryContainer = Color(0xFFFFEAE3),
    onSecondaryContainer = Color(0xFF3B0900),
    tertiary = Color(0xFF705A2C),
    onTertiary = Color.White,
    background = BackgroundLight,
    onBackground = Color(0xFF201A15),
    surface = SurfaceLight,
    onSurface = Color(0xFF201A15),
    surfaceVariant = Color(0xFFF4DED3),
    onSurfaceVariant = Color(0xFF52443C),
    error = ErrorLight,
    onError = Color.White,
    outline = Color(0xFF85736B),
)

private val DarkColorScheme = darkColorScheme(
    primary = OrangePrimaryDark,
    onPrimary = Color(0xFF502400),
    primaryContainer = OrangeContainerDark,
    onPrimaryContainer = OrangeOnContainerDark,
    secondary = OrangeSecondaryDark,
    onSecondary = Color(0xFF5C1900),
    secondaryContainer = Color(0xFF82300F),
    onSecondaryContainer = Color(0xFFFFDBCF),
    tertiary = Color(0xFFDEC38F),
    onTertiary = Color(0xFF3E2E04),
    background = BackgroundDark,
    onBackground = Color(0xFFEDE0D9),
    surface = SurfaceDark,
    onSurface = Color(0xFFEDE0D9),
    surfaceVariant = Color(0xFF52443C),
    onSurfaceVariant = Color(0xFFD7C2B8),
    error = ErrorDark,
    onError = Color(0xFF690005),
    outline = Color(0xFF9F8D83),
)

@Composable
fun SpesaFacileTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit,
) {
    val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
