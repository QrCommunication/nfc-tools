package com.nfcemulator.ui.theme

import androidx.compose.ui.graphics.Color

interface NfcColorPalette {
    // MD3 Core
    val Primary: Color
    val Secondary: Color
    val Accent: Color
    val Background: Color
    val Surface: Color
    val SurfaceVariant: Color
    val SurfaceContainer: Color
    val SurfaceContainerHigh: Color
    val TextPrimary: Color
    val TextSecondary: Color
    val Border: Color
    val BorderActive: Color
    val Error: Color
    val Warning: Color
    val Success: Color
    val Info: Color

    // NFC-specific
    val NfcPulse: Color
    val NfcReading: Color
    val KeyFound: Color
    val KeyMissing: Color
    val EmulationActive: Color
    val RootActive: Color
    val HceOnly: Color
    val HexDefault: Color
    val HexModified: Color
    val HexAccessBits: Color
    val HexKeyA: Color
    val HexKeyB: Color
}

/**
 * Dark theme — Electric Cyberpunk MD3
 * Seed: Electric Cyan #00E5FF
 * Palette: Cyan / Neon Mint / Vivid Purple
 */
object NfcColors : NfcColorPalette {
    // Primary — Electric Cyan
    override val Primary = Color(0xFF00E5FF)
    // Secondary — Neon Mint Green
    override val Secondary = Color(0xFF00FF87)
    // Tertiary — Vivid Purple
    override val Accent = Color(0xFFD500F9)

    // Surfaces — deep indigo-black for AMOLED
    override val Background = Color(0xFF04040C)
    override val Surface = Color(0xFF0C0C18)
    override val SurfaceVariant = Color(0xFF1A1B2E)
    override val SurfaceContainer = Color(0xFF121222)
    override val SurfaceContainerHigh = Color(0xFF222236)

    // Text
    override val TextPrimary = Color(0xFFEAEAF6)
    override val TextSecondary = Color(0xFF8888AA)

    // Borders
    override val Border = Color(0xFF262642)
    override val BorderActive = Color(0xFF00E5FF)

    // Semantic
    override val Error = Color(0xFFFF1744)
    override val Warning = Color(0xFFFFEA00)
    override val Success = Color(0xFF00FF87)
    override val Info = Color(0xFF00E5FF)

    // NFC states
    override val NfcPulse = Color(0xFF00E5FF)
    override val NfcReading = Color(0xFF00E5FF)
    override val KeyFound = Color(0xFF00FF87)
    override val KeyMissing = Color(0xFFFF1744)
    override val EmulationActive = Color(0xFF00FF87)
    override val RootActive = Color(0xFFD500F9)
    override val HceOnly = Color(0xFFFFEA00)

    // Hex editor
    override val HexDefault = Color(0xFF00E5FF)
    override val HexModified = Color(0xFFD500F9)
    override val HexAccessBits = Color(0xFFFFEA00)
    override val HexKeyA = Color(0xFF00FF87)
    override val HexKeyB = Color(0xFF448AFF)
}

/**
 * Light theme — Vibrant Teal MD3
 * Rich saturated colors for daytime readability
 */
object NfcLightColors : NfcColorPalette {
    // Primary — Rich Teal
    override val Primary = Color(0xFF00838F)
    // Secondary — Deep Mint
    override val Secondary = Color(0xFF00796B)
    // Tertiary — Deep Purple
    override val Accent = Color(0xFFAA00FF)

    // Surfaces — cool white
    override val Background = Color(0xFFF0F4FA)
    override val Surface = Color(0xFFFFFFFF)
    override val SurfaceVariant = Color(0xFFE0E6F0)
    override val SurfaceContainer = Color(0xFFEAEFF6)
    override val SurfaceContainerHigh = Color(0xFFDCE2EE)

    // Text
    override val TextPrimary = Color(0xFF0F172A)
    override val TextSecondary = Color(0xFF64748B)

    // Borders
    override val Border = Color(0xFFCBD5E1)
    override val BorderActive = Color(0xFF00838F)

    // Semantic
    override val Error = Color(0xFFDC2626)
    override val Warning = Color(0xFFD97706)
    override val Success = Color(0xFF00796B)
    override val Info = Color(0xFF00838F)

    // NFC states
    override val NfcPulse = Color(0xFF00838F)
    override val NfcReading = Color(0xFF00838F)
    override val KeyFound = Color(0xFF00796B)
    override val KeyMissing = Color(0xFFDC2626)
    override val EmulationActive = Color(0xFF00796B)
    override val RootActive = Color(0xFFAA00FF)
    override val HceOnly = Color(0xFFD97706)

    // Hex editor
    override val HexDefault = Color(0xFF006064)
    override val HexModified = Color(0xFFAA00FF)
    override val HexAccessBits = Color(0xFFD97706)
    override val HexKeyA = Color(0xFF00796B)
    override val HexKeyB = Color(0xFF1565C0)
}
