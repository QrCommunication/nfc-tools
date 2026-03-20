package com.nfcemulator.ui.theme

import androidx.compose.ui.graphics.Color

interface NfcColorPalette {
    val Primary: Color
    val Secondary: Color
    val Accent: Color
    val Background: Color
    val Surface: Color
    val SurfaceVariant: Color
    val TextPrimary: Color
    val TextSecondary: Color
    val Border: Color
    val BorderActive: Color
    val Error: Color
    val Warning: Color
    val Success: Color
    val Info: Color
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

object NfcColors : NfcColorPalette {
    override val Primary = Color(0xFF00FFFF)
    override val Secondary = Color(0xFF00FF41)
    override val Accent = Color(0xFFFF00FF)
    override val Background = Color(0xFF000000)
    override val Surface = Color(0xFF0D0D0D)
    override val SurfaceVariant = Color(0xFF1A1A1A)
    override val TextPrimary = Color(0xFFE0E0E0)
    override val TextSecondary = Color(0xFF808080)
    override val Border = Color(0xFF1F1F1F)
    override val BorderActive = Color(0xFF00FFFF)
    override val Error = Color(0xFFFF3B30)
    override val Warning = Color(0xFFFFB800)
    override val Success = Color(0xFF00FF41)
    override val Info = Color(0xFF00FFFF)
    override val NfcPulse = Color(0xFF00FFFF)
    override val NfcReading = Color(0xFF00FFFF)
    override val KeyFound = Color(0xFF00FF41)
    override val KeyMissing = Color(0xFFFF3B30)
    override val EmulationActive = Color(0xFF00FF41)
    override val RootActive = Color(0xFFFF00FF)
    override val HceOnly = Color(0xFFFFB800)
    override val HexDefault = Color(0xFF00FFFF)
    override val HexModified = Color(0xFFFF00FF)
    override val HexAccessBits = Color(0xFFFFB800)
    override val HexKeyA = Color(0xFF00FF41)
    override val HexKeyB = Color(0xFF60A5FA)
}

object NfcLightColors : NfcColorPalette {
    override val Primary = Color(0xFF0097A7)
    override val Secondary = Color(0xFF00897B)
    override val Accent = Color(0xFFD500F9)
    override val Background = Color(0xFFF5F5F5)
    override val Surface = Color(0xFFFFFFFF)
    override val SurfaceVariant = Color(0xFFEEEEEE)
    override val TextPrimary = Color(0xFF212121)
    override val TextSecondary = Color(0xFF757575)
    override val Border = Color(0xFFE0E0E0)
    override val BorderActive = Color(0xFF0097A7)
    override val Error = Color(0xFFD32F2F)
    override val Warning = Color(0xFFF57F17)
    override val Success = Color(0xFF2E7D32)
    override val Info = Color(0xFF0097A7)
    override val NfcPulse = Color(0xFF0097A7)
    override val NfcReading = Color(0xFF0097A7)
    override val KeyFound = Color(0xFF2E7D32)
    override val KeyMissing = Color(0xFFD32F2F)
    override val EmulationActive = Color(0xFF2E7D32)
    override val RootActive = Color(0xFFD500F9)
    override val HceOnly = Color(0xFFF57F17)
    override val HexDefault = Color(0xFF006064)
    override val HexModified = Color(0xFFD500F9)
    override val HexAccessBits = Color(0xFFF57F17)
    override val HexKeyA = Color(0xFF2E7D32)
    override val HexKeyB = Color(0xFF1565C0)
}
