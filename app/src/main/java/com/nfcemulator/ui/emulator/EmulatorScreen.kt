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
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.unit.dp
import com.nfcemulator.ui.home.TagUiModel
import com.nfcemulator.ui.theme.NfcColors
import com.nfcemulator.ui.theme.NfcDimensions
import com.nfcemulator.ui.theme.NfcMonoStyles

@Composable
fun EmulatorScreen(
    selectedTag: TagUiModel?,
    isEmulating: Boolean,
    emulationMode: String,
    onStartEmulation: () -> Unit,
    onStopEmulation: () -> Unit,
    onSelectTag: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(NfcColors.Background)
            .padding(NfcDimensions.Padding),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text("Emulate", style = MaterialTheme.typography.headlineLarge, color = NfcColors.Primary)

        Spacer(modifier = Modifier.height(8.dp))

        Surface(
            color = NfcColors.SurfaceVariant,
            shape = RoundedCornerShape(8.dp)
        ) {
            Text(
                text = emulationMode,
                style = MaterialTheme.typography.labelMedium,
                color = if (emulationMode.contains("Full")) NfcColors.RootActive else NfcColors.HceOnly,
                modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp)
            )
        }

        Spacer(modifier = Modifier.weight(1f))

        if (selectedTag != null) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(NfcColors.Surface, RoundedCornerShape(NfcDimensions.CornerRadius))
                    .border(NfcDimensions.BorderWidth, if (isEmulating) NfcColors.EmulationActive else NfcColors.Border, RoundedCornerShape(NfcDimensions.CornerRadius))
                    .padding(NfcDimensions.CardPadding),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(selectedTag.name, style = MaterialTheme.typography.titleLarge, color = NfcColors.TextPrimary)
                Spacer(modifier = Modifier.height(4.dp))
                Text("UID: ${selectedTag.uid}", style = NfcMonoStyles.uid, color = NfcColors.Secondary)
                Spacer(modifier = Modifier.height(4.dp))
                Text(selectedTag.type, style = MaterialTheme.typography.bodySmall, color = NfcColors.TextSecondary)
            }

            Spacer(modifier = Modifier.height(32.dp))

            val buttonColor by animateColorAsState(
                targetValue = if (isEmulating) NfcColors.Error else NfcColors.Primary,
                label = "buttonColor"
            )

            Button(
                onClick = if (isEmulating) onStopEmulation else onStartEmulation,
                colors = ButtonDefaults.buttonColors(containerColor = buttonColor),
                shape = CircleShape,
                modifier = Modifier.size(120.dp)
            ) {
                Text(
                    text = if (isEmulating) "STOP" else "START",
                    style = MaterialTheme.typography.titleLarge,
                    color = NfcColors.Background
                )
            }

            if (isEmulating) {
                Spacer(modifier = Modifier.height(16.dp))
                Text("Emulation active — hold phone near reader", style = MaterialTheme.typography.bodyMedium, color = NfcColors.EmulationActive)
            }
        } else {
            Text("No tag selected", style = MaterialTheme.typography.titleMedium, color = NfcColors.TextSecondary)
            Spacer(modifier = Modifier.height(16.dp))
            OutlinedButton(
                onClick = onSelectTag,
                colors = ButtonDefaults.outlinedButtonColors(contentColor = NfcColors.Primary),
                border = ButtonDefaults.outlinedButtonBorder.copy(brush = androidx.compose.ui.graphics.SolidColor(NfcColors.Primary))
            ) {
                Text("Select a tag")
            }
        }

        Spacer(modifier = Modifier.weight(1f))
    }
}
