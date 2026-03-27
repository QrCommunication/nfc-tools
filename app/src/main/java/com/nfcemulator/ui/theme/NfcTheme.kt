package com.nfcemulator.ui.theme

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Shapes
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.compositionLocalOf
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

val LocalIsDarkTheme = compositionLocalOf { true }
val LocalAppColors = staticCompositionLocalOf<NfcColorPalette> { NfcColors }

// MD3 Dark Color Scheme — Electric Cyberpunk
private val NfcDarkColorScheme = darkColorScheme(
    primary = NfcColors.Primary,
    onPrimary = Color(0xFF003640),
    primaryContainer = Color(0xFF004D5C),
    onPrimaryContainer = Color(0xFF6FF7FF),
    secondary = NfcColors.Secondary,
    onSecondary = Color(0xFF003822),
    secondaryContainer = Color(0xFF005234),
    onSecondaryContainer = Color(0xFF95F8C9),
    tertiary = NfcColors.Accent,
    onTertiary = Color(0xFF4A0072),
    tertiaryContainer = Color(0xFF6A1B9A),
    onTertiaryContainer = Color(0xFFF3D1FF),
    error = NfcColors.Error,
    onError = Color(0xFFFFFFFF),
    errorContainer = Color(0xFF93000A),
    onErrorContainer = Color(0xFFFFB4AB),
    background = NfcColors.Background,
    onBackground = NfcColors.TextPrimary,
    surface = NfcColors.Surface,
    onSurface = NfcColors.TextPrimary,
    surfaceVariant = NfcColors.SurfaceVariant,
    onSurfaceVariant = NfcColors.TextSecondary,
    surfaceTint = NfcColors.Primary,
    outline = NfcColors.Border,
    outlineVariant = Color(0xFF1E1E36),
    inverseSurface = Color(0xFFE3E3F0),
    inverseOnSurface = Color(0xFF1A1A2E),
    inversePrimary = Color(0xFF006874),
    scrim = Color(0xFF000000),
    surfaceBright = Color(0xFF2A2A3E),
    surfaceDim = NfcColors.Background,
    surfaceContainer = NfcColors.SurfaceContainer,
    surfaceContainerHigh = NfcColors.SurfaceContainerHigh,
    surfaceContainerHighest = Color(0xFF2E2E44),
    surfaceContainerLow = Color(0xFF0A0A16),
    surfaceContainerLowest = Color(0xFF020208)
)

// MD3 Light Color Scheme — Vibrant Teal
private val NfcLightColorScheme = lightColorScheme(
    primary = NfcLightColors.Primary,
    onPrimary = Color(0xFFFFFFFF),
    primaryContainer = Color(0xFF97F0FF),
    onPrimaryContainer = Color(0xFF001F24),
    secondary = NfcLightColors.Secondary,
    onSecondary = Color(0xFFFFFFFF),
    secondaryContainer = Color(0xFF7CFCB7),
    onSecondaryContainer = Color(0xFF00210F),
    tertiary = NfcLightColors.Accent,
    onTertiary = Color(0xFFFFFFFF),
    tertiaryContainer = Color(0xFFF3D1FF),
    onTertiaryContainer = Color(0xFF320047),
    error = NfcLightColors.Error,
    onError = Color(0xFFFFFFFF),
    errorContainer = Color(0xFFFFDAD6),
    onErrorContainer = Color(0xFF410002),
    background = NfcLightColors.Background,
    onBackground = NfcLightColors.TextPrimary,
    surface = NfcLightColors.Surface,
    onSurface = NfcLightColors.TextPrimary,
    surfaceVariant = NfcLightColors.SurfaceVariant,
    onSurfaceVariant = NfcLightColors.TextSecondary,
    surfaceTint = NfcLightColors.Primary,
    outline = NfcLightColors.Border,
    outlineVariant = Color(0xFFE0E6F0),
    inverseSurface = Color(0xFF2F3140),
    inverseOnSurface = Color(0xFFF0F0FA),
    inversePrimary = Color(0xFF00E5FF),
    scrim = Color(0xFF000000),
    surfaceBright = Color(0xFFFFFFFF),
    surfaceDim = Color(0xFFD8DEE8),
    surfaceContainer = NfcLightColors.SurfaceContainer,
    surfaceContainerHigh = NfcLightColors.SurfaceContainerHigh,
    surfaceContainerHighest = Color(0xFFD0D6E2),
    surfaceContainerLow = Color(0xFFF2F6FC),
    surfaceContainerLowest = Color(0xFFFFFFFF)
)

// MD3 Shape system
val NfcShapes = Shapes(
    extraSmall = RoundedCornerShape(4.dp),
    small = RoundedCornerShape(8.dp),
    medium = RoundedCornerShape(12.dp),
    large = RoundedCornerShape(16.dp),
    extraLarge = RoundedCornerShape(28.dp)
)

@Composable
fun NfcEmulatorTheme(
    darkTheme: Boolean = true,
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) NfcDarkColorScheme else NfcLightColorScheme
    val appColors: NfcColorPalette = if (darkTheme) NfcColors else NfcLightColors

    CompositionLocalProvider(
        LocalIsDarkTheme provides darkTheme,
        LocalAppColors provides appColors
    ) {
        MaterialTheme(
            colorScheme = colorScheme,
            typography = NfcTypography,
            shapes = NfcShapes,
            content = content
        )
    }
}
