package com.example.spesafacile.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Shapes
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

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
    surfaceTint = OrangePrimary,
    surfaceDim = Color(0xFFE9DFD1),
    surfaceBright = Color(0xFFFFFDF6),
    surfaceContainerLowest = Color(0xFFFFFFFF),
    surfaceContainerLow = Color(0xFFFFFAF2),
    surfaceContainer = Color(0xFFFFF6EA),
    surfaceContainerHigh = Color(0xFFFFF2E2),
    surfaceContainerHighest = Color(0xFFF8E8D3),
    tertiaryContainer = Color(0xFFF1E5C8),
    onTertiaryContainer = Color(0xFF322B18),
    outlineVariant = Color(0xFFDCC9B4),
    inverseSurface = Color(0xFF382F27),
    inverseOnSurface = Color(0xFFFFF2E2),
    inversePrimary = OrangePrimaryDark,
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
    surfaceTint = OrangePrimaryDark,
    surfaceDim = Color(0xFF181410),
    surfaceBright = Color(0xFF41372E),
    surfaceContainerLowest = Color(0xFF15120F),
    surfaceContainerLow = Color(0xFF241E19),
    surfaceContainer = Color(0xFF2B241E),
    surfaceContainerHigh = Color(0xFF342B23),
    surfaceContainerHighest = Color(0xFF3D3229),
    tertiaryContainer = Color(0xFF51462C),
    onTertiaryContainer = Color(0xFFF0DFB4),
    outlineVariant = Color(0xFF655344),
    inverseSurface = Color(0xFFEDE0D0),
    inverseOnSurface = Color(0xFF30271F),
    inversePrimary = OrangePrimary,
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
        shapes = Shapes(
            small = RoundedCornerShape(8.dp),
            medium = RoundedCornerShape(12.dp),
            large = RoundedCornerShape(14.dp),
            extraLarge = RoundedCornerShape(14.dp)
        ),
        content = content
    )
}
