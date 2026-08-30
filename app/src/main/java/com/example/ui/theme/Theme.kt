package com.example.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

private val LightColorScheme = lightColorScheme(
    primary = ElectricPurple,
    onPrimary = Color.White,
    primaryContainer = ElectricPurpleLight,
    onPrimaryContainer = ElectricPurple,
    secondary = NearBlackInk,
    onSecondary = Color.White,
    secondaryContainer = ElectricPurpleContainer,
    onSecondaryContainer = NearBlackInk,
    tertiary = AccentPeach,
    background = SoftWhiteBackground,
    onBackground = NearBlackInk,
    surface = PureWhiteSurface,
    onSurface = NearBlackInk,
    surfaceVariant = MutedSurface,
    onSurfaceVariant = TextSecondary,
    outline = SubtleBorder
)

private val DarkColorScheme = darkColorScheme(
    primary = ElectricPurple,
    onPrimary = Color.White,
    primaryContainer = Color(0xFF2E1065),
    onPrimaryContainer = ElectricPurpleLight,
    secondary = Color(0xFFE4E4E7),
    onSecondary = Color.Black,
    secondaryContainer = Color(0xFF3F3F46),
    onSecondaryContainer = Color.White,
    tertiary = AccentPeachLight,
    background = Color(0xFF121212),
    onBackground = Color(0xFFF4F4F5),
    surface = Color(0xFF18181B),
    onSurface = Color(0xFFF4F4F5),
    surfaceVariant = Color(0xFF27272A),
    onSurfaceVariant = Color(0xFFA1A1AA),
    outline = Color(0xFF3F3F46)
)

val AppShapes = Shapes(
    small = RoundedCornerShape(12.dp),
    medium = RoundedCornerShape(14.dp),
    large = RoundedCornerShape(16.dp),
    extraLarge = RoundedCornerShape(16.dp)
)

@Composable
fun MyApplicationTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = false,
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme
    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        shapes = AppShapes,
        content = content
    )
}
