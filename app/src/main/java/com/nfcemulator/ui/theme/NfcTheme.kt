package com.nfcemulator.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.compositionLocalOf
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color

val LocalIsDarkTheme = compositionLocalOf { true }
val LocalAppColors = staticCompositionLocalOf<NfcColorPalette> { NfcColors }

private val NfcDarkColorScheme = darkColorScheme(
    primary = NfcColors.Primary,
    onPrimary = NfcColors.Background,
    primaryContainer = NfcColors.SurfaceVariant,
    onPrimaryContainer = NfcColors.Primary,
    secondary = NfcColors.Secondary,
    onSecondary = NfcColors.Background,
    secondaryContainer = NfcColors.SurfaceVariant,
    onSecondaryContainer = NfcColors.Secondary,
    tertiary = NfcColors.Accent,
    onTertiary = NfcColors.Background,
    error = NfcColors.Error,
    onError = NfcColors.TextPrimary,
    background = NfcColors.Background,
    onBackground = NfcColors.TextPrimary,
    surface = NfcColors.Surface,
    onSurface = NfcColors.TextPrimary,
    surfaceVariant = NfcColors.SurfaceVariant,
    onSurfaceVariant = NfcColors.TextSecondary,
    outline = NfcColors.Border,
    outlineVariant = NfcColors.Border
)

private val NfcLightColorScheme = lightColorScheme(
    primary = NfcLightColors.Primary,
    onPrimary = Color(0xFFFFFFFF),
    primaryContainer = NfcLightColors.SurfaceVariant,
    onPrimaryContainer = NfcLightColors.Primary,
    secondary = NfcLightColors.Secondary,
    onSecondary = Color(0xFFFFFFFF),
    tertiary = NfcLightColors.Accent,
    error = NfcLightColors.Error,
    background = NfcLightColors.Background,
    onBackground = NfcLightColors.TextPrimary,
    surface = NfcLightColors.Surface,
    onSurface = NfcLightColors.TextPrimary,
    surfaceVariant = NfcLightColors.SurfaceVariant,
    onSurfaceVariant = NfcLightColors.TextSecondary,
    outline = NfcLightColors.Border
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
            content = content
        )
    }
}
