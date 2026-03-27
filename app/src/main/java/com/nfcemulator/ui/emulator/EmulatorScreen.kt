package com.nfcemulator.ui.emulator

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.nfcemulator.R
import com.nfcemulator.ui.home.TagUiModel
import com.nfcemulator.ui.theme.LocalAppColors
import com.nfcemulator.ui.theme.NfcDimensions
import com.nfcemulator.ui.theme.NfcMonoStyles

@Composable
fun EmulatorScreen(
    selectedTag: TagUiModel?,
    isEmulating: Boolean,
    emulationMode: String,
    statusMessage: String = "",
    onStartEmulation: () -> Unit,
    onStopEmulation: () -> Unit,
    onSelectTag: () -> Unit
) {
    val colors = LocalAppColors.current

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(colors.Background)
            .padding(NfcDimensions.Padding),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(stringResource(R.string.emulate_title), style = MaterialTheme.typography.headlineLarge, color = colors.Primary)

        Spacer(modifier = Modifier.height(8.dp))

        // Mode badge — pill shape
        Surface(
            color = colors.SurfaceContainerHigh,
            shape = RoundedCornerShape(50)
        ) {
            Text(
                text = emulationMode,
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.SemiBold,
                color = if (emulationMode.contains("Full")) colors.RootActive else colors.HceOnly,
                modifier = Modifier.padding(horizontal = 14.dp, vertical = 5.dp)
            )
        }

        Spacer(modifier = Modifier.weight(1f))

        if (selectedTag != null) {
            // Tag card with ElevatedCard-style + animated EmulationActive border when emulating
            val borderColor by animateColorAsState(
                targetValue = if (isEmulating) colors.EmulationActive else colors.Border,
                animationSpec = tween(400),
                label = "borderColor"
            )
            val borderWidth = if (isEmulating) 2.dp else NfcDimensions.BorderWidth

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(colors.SurfaceContainer, RoundedCornerShape(NfcDimensions.CornerRadius))
                    .border(borderWidth, borderColor, RoundedCornerShape(NfcDimensions.CornerRadius))
                    .padding(NfcDimensions.CardPadding),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(selectedTag.name, style = MaterialTheme.typography.titleLarge, color = colors.TextPrimary, fontWeight = FontWeight.SemiBold)
                Spacer(modifier = Modifier.height(4.dp))
                Text("UID: ${selectedTag.uid}", style = NfcMonoStyles.uid, color = colors.Secondary)
                Spacer(modifier = Modifier.height(4.dp))
                Text(selectedTag.type, style = MaterialTheme.typography.bodySmall, color = colors.TextSecondary)
            }

            Spacer(modifier = Modifier.height(32.dp))

            // Large circular FAB-style button with pulse animation when emulating
            val infiniteTransition = rememberInfiniteTransition(label = "emulatingPulse")
            val pulseScale by infiniteTransition.animateFloat(
                initialValue = 1f,
                targetValue = if (isEmulating) 1.05f else 1f,
                animationSpec = infiniteRepeatable(
                    animation = tween(700, easing = FastOutSlowInEasing),
                    repeatMode = RepeatMode.Reverse
                ),
                label = "pulseScale"
            )

            val buttonColor by animateColorAsState(
                targetValue = if (isEmulating) colors.Error else colors.Primary,
                label = "buttonColor"
            )

            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier.scale(pulseScale)
            ) {
                Button(
                    onClick = if (isEmulating) onStopEmulation else onStartEmulation,
                    colors = ButtonDefaults.buttonColors(containerColor = buttonColor),
                    shape = CircleShape,
                    modifier = Modifier.size(96.dp),
                    contentPadding = PaddingValues(0.dp)
                ) {
                    Text(
                        text = if (isEmulating) stringResource(R.string.stop) else stringResource(R.string.start),
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = colors.Background
                    )
                }
            }

            if (statusMessage.isNotEmpty()) {
                Spacer(modifier = Modifier.height(16.dp))
                val messageColor = when {
                    statusMessage.startsWith("Error") -> colors.Error
                    statusMessage.startsWith("Unsupported") -> colors.Warning
                    statusMessage.contains("active") -> colors.EmulationActive
                    else -> colors.TextSecondary
                }
                // Status pill/badge
                Surface(
                    color = messageColor.copy(alpha = 0.12f),
                    shape = RoundedCornerShape(50)
                ) {
                    Text(
                        statusMessage,
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Medium,
                        color = messageColor,
                        modifier = Modifier.padding(horizontal = 14.dp, vertical = 6.dp)
                    )
                }
            }
        } else {
            Text(stringResource(R.string.no_tag_selected), style = MaterialTheme.typography.titleMedium, color = colors.TextSecondary)
            Spacer(modifier = Modifier.height(16.dp))
            Button(
                onClick = onSelectTag,
                colors = ButtonDefaults.buttonColors(
                    containerColor = colors.Primary,
                    contentColor = colors.Background
                ),
                shape = RoundedCornerShape(28.dp),
                modifier = Modifier.height(48.dp)
            ) {
                Text(stringResource(R.string.select_a_tag), fontWeight = FontWeight.SemiBold)
            }
        }

        Spacer(modifier = Modifier.weight(1f))
    }
}
