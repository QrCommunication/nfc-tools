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

object NfcLightColors {
    val Primary = Color(0xFF0097A7)
    val Secondary = Color(0xFF00897B)
    val Accent = Color(0xFFD500F9)
    val Background = Color(0xFFF5F5F5)
    val Surface = Color(0xFFFFFFFF)
    val SurfaceVariant = Color(0xFFEEEEEE)
    val TextPrimary = Color(0xFF212121)
    val TextSecondary = Color(0xFF757575)
    val Border = Color(0xFFE0E0E0)
    val BorderActive = Color(0xFF0097A7)
    val Error = Color(0xFFD32F2F)
    val Warning = Color(0xFFF57F17)
    val Success = Color(0xFF2E7D32)
    val Info = Color(0xFF0097A7)
    val NfcPulse = Color(0xFF0097A7)
    val NfcReading = Color(0xFF0097A7)
    val KeyFound = Color(0xFF2E7D32)
    val KeyMissing = Color(0xFFD32F2F)
    val EmulationActive = Color(0xFF2E7D32)
    val RootActive = Color(0xFFD500F9)
    val HceOnly = Color(0xFFF57F17)
    val HexDefault = Color(0xFF006064)
    val HexModified = Color(0xFFD500F9)
    val HexAccessBits = Color(0xFFF57F17)
    val HexKeyA = Color(0xFF2E7D32)
    val HexKeyB = Color(0xFF1565C0)
}
