package com.nfcemulator.ui.theme

import androidx.compose.material3.Typography
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import com.nfcemulator.R

val Exo2Family = FontFamily(
    Font(R.font.exo2_regular, FontWeight.Normal),
    Font(R.font.exo2_medium, FontWeight.Medium),
    Font(R.font.exo2_semibold, FontWeight.SemiBold),
    Font(R.font.exo2_bold, FontWeight.Bold)
)

val JetBrainsMonoFamily = FontFamily(
    Font(R.font.jetbrainsmono_regular, FontWeight.Normal),
    Font(R.font.jetbrainsmono_bold, FontWeight.Bold)
)

val NfcTypography = Typography(
    displayLarge = TextStyle(
        fontFamily = Exo2Family,
        fontWeight = FontWeight.Bold,
        fontSize = 32.sp,
        lineHeight = 40.sp,
        color = NfcColors.TextPrimary
    ),
    headlineLarge = TextStyle(
        fontFamily = Exo2Family,
        fontWeight = FontWeight.Bold,
        fontSize = 28.sp,
        lineHeight = 36.sp,
        color = NfcColors.TextPrimary
    ),
    headlineMedium = TextStyle(
        fontFamily = Exo2Family,
        fontWeight = FontWeight.SemiBold,
        fontSize = 22.sp,
        lineHeight = 28.sp,
        color = NfcColors.TextPrimary
    ),
    headlineSmall = TextStyle(
        fontFamily = Exo2Family,
        fontWeight = FontWeight.Medium,
        fontSize = 18.sp,
        lineHeight = 24.sp,
        color = NfcColors.TextPrimary
    ),
    titleLarge = TextStyle(
        fontFamily = Exo2Family,
        fontWeight = FontWeight.SemiBold,
        fontSize = 20.sp,
        lineHeight = 26.sp,
        color = NfcColors.TextPrimary
    ),
    titleMedium = TextStyle(
        fontFamily = Exo2Family,
        fontWeight = FontWeight.Medium,
        fontSize = 16.sp,
        lineHeight = 22.sp,
        color = NfcColors.TextPrimary
    ),
    bodyLarge = TextStyle(
        fontFamily = Exo2Family,
        fontWeight = FontWeight.Normal,
        fontSize = 16.sp,
        lineHeight = 24.sp,
        color = NfcColors.TextPrimary
    ),
    bodyMedium = TextStyle(
        fontFamily = Exo2Family,
        fontWeight = FontWeight.Normal,
        fontSize = 14.sp,
        lineHeight = 20.sp,
        color = NfcColors.TextPrimary
    ),
    bodySmall = TextStyle(
        fontFamily = Exo2Family,
        fontWeight = FontWeight.Normal,
        fontSize = 12.sp,
        lineHeight = 16.sp,
        color = NfcColors.TextSecondary
    ),
    labelLarge = TextStyle(
        fontFamily = Exo2Family,
        fontWeight = FontWeight.SemiBold,
        fontSize = 14.sp,
        lineHeight = 20.sp,
        color = NfcColors.TextPrimary
    ),
    labelMedium = TextStyle(
        fontFamily = Exo2Family,
        fontWeight = FontWeight.Medium,
        fontSize = 12.sp,
        lineHeight = 16.sp,
        color = NfcColors.TextSecondary
    ),
    labelSmall = TextStyle(
        fontFamily = Exo2Family,
        fontWeight = FontWeight.Medium,
        fontSize = 10.sp,
        lineHeight = 14.sp,
        color = NfcColors.TextSecondary
    )
)

object NfcMonoStyles {
    val hexData = TextStyle(
        fontFamily = JetBrainsMonoFamily,
        fontWeight = FontWeight.Normal,
        fontSize = 14.sp,
        lineHeight = 20.sp,
        color = NfcColors.HexDefault
    )
    val hexDataBold = TextStyle(
        fontFamily = JetBrainsMonoFamily,
        fontWeight = FontWeight.Bold,
        fontSize = 14.sp,
        lineHeight = 20.sp,
        color = NfcColors.HexDefault
    )
    val uid = TextStyle(
        fontFamily = JetBrainsMonoFamily,
        fontWeight = FontWeight.Bold,
        fontSize = 16.sp,
        lineHeight = 22.sp,
        color = NfcColors.Secondary
    )
    val key = TextStyle(
        fontFamily = JetBrainsMonoFamily,
        fontWeight = FontWeight.Normal,
        fontSize = 14.sp,
        lineHeight = 20.sp,
        color = NfcColors.HexKeyA
    )
}
