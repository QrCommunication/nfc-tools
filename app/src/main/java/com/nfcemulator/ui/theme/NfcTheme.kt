package com.nfcemulator.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable

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

@Composable
fun NfcEmulatorTheme(
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = NfcDarkColorScheme,
        typography = NfcTypography,
        content = content
    )
}
