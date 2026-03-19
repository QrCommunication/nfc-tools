package com.nfcemulator.ui.theme

import androidx.compose.ui.graphics.Color

object NfcColors {
    // Primary palette
    val Primary = Color(0xFF00FFFF)        // Neon Cyan
    val Secondary = Color(0xFF00FF41)       // Matrix Green
    val Accent = Color(0xFFFF00FF)          // Neon Magenta

    // Backgrounds
    val Background = Color(0xFF000000)      // Deep Black (OLED)
    val Surface = Color(0xFF0D0D0D)         // Dark Surface
    val SurfaceVariant = Color(0xFF1A1A1A)  // Charcoal

    // Text
    val TextPrimary = Color(0xFFE0E0E0)     // Light Gray
    val TextSecondary = Color(0xFF808080)   // Medium Gray

    // Borders
    val Border = Color(0xFF1F1F1F)          // Dark Border
    val BorderActive = Color(0xFF00FFFF)    // Cyan border (active)

    // Semantic
    val Error = Color(0xFFFF3B30)           // Alert Red
    val Warning = Color(0xFFFFB800)         // Amber
    val Success = Color(0xFF00FF41)         // Matrix Green
    val Info = Color(0xFF00FFFF)            // Neon Cyan

    // NFC-specific
    val NfcPulse = Color(0xFF00FFFF)        // Tag detected
    val NfcReading = Color(0xFF00FFFF)      // Reading
    val KeyFound = Color(0xFF00FF41)        // Key found
    val KeyMissing = Color(0xFFFF3B30)      // Key missing
    val EmulationActive = Color(0xFF00FF41) // Emulating
    val RootActive = Color(0xFFFF00FF)      // Root mode
    val HceOnly = Color(0xFFFFB800)         // HCE limited

    // Hex editor
    val HexDefault = Color(0xFF00FFFF)      // Default hex data
    val HexModified = Color(0xFFFF00FF)     // Modified data
    val HexAccessBits = Color(0xFFFFB800)   // Access bits
    val HexKeyA = Color(0xFF00FF41)         // Key A
    val HexKeyB = Color(0xFF60A5FA)         // Key B
}
