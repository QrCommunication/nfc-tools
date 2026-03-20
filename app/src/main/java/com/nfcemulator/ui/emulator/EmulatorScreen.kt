package com.nfcemulator.ui.emulator

import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
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
    writeProgress: String = "",
    onStartEmulation: () -> Unit,
    onStopEmulation: () -> Unit,
    onWriteToTag: () -> Unit,
    onSelectTag: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(LocalAppColors.current.Background)
            .padding(NfcDimensions.Padding),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(stringResource(R.string.emulate_title), style = MaterialTheme.typography.headlineLarge, color = LocalAppColors.current.Primary)

        Spacer(modifier = Modifier.height(8.dp))

        Surface(
            color = LocalAppColors.current.SurfaceVariant,
            shape = RoundedCornerShape(8.dp)
        ) {
            Text(
                text = emulationMode,
                style = MaterialTheme.typography.labelMedium,
                color = if (emulationMode.contains("Full")) LocalAppColors.current.RootActive else LocalAppColors.current.HceOnly,
                modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp)
            )
        }

        Spacer(modifier = Modifier.weight(1f))

        if (selectedTag != null) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(LocalAppColors.current.Surface, RoundedCornerShape(NfcDimensions.CornerRadius))
                    .border(
                        NfcDimensions.BorderWidth,
                        if (isEmulating) LocalAppColors.current.EmulationActive else LocalAppColors.current.Border,
                        RoundedCornerShape(NfcDimensions.CornerRadius)
                    )
                    .padding(NfcDimensions.CardPadding),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(selectedTag.name, style = MaterialTheme.typography.titleLarge, color = LocalAppColors.current.TextPrimary)
                Spacer(modifier = Modifier.height(4.dp))
                Text("UID: ${selectedTag.uid}", style = NfcMonoStyles.uid, color = LocalAppColors.current.Secondary)
                Spacer(modifier = Modifier.height(4.dp))
                Text(selectedTag.type, style = MaterialTheme.typography.bodySmall, color = LocalAppColors.current.TextSecondary)
            }

            Spacer(modifier = Modifier.height(32.dp))

            val buttonColor by animateColorAsState(
                targetValue = if (isEmulating) LocalAppColors.current.Error else LocalAppColors.current.Primary,
                label = "buttonColor"
            )

            Button(
                onClick = if (isEmulating) onStopEmulation else onStartEmulation,
                colors = ButtonDefaults.buttonColors(containerColor = buttonColor),
                shape = CircleShape,
                modifier = Modifier.size(120.dp)
            ) {
                Text(
                    text = if (isEmulating) stringResource(R.string.stop) else stringResource(R.string.start),
                    style = MaterialTheme.typography.titleLarge,
                    color = LocalAppColors.current.Background
                )
            }

            if (statusMessage.isNotEmpty()) {
                Spacer(modifier = Modifier.height(16.dp))
                val messageColor = when {
                    statusMessage.startsWith("Error") -> LocalAppColors.current.Error
                    statusMessage.startsWith("Unsupported") -> LocalAppColors.current.Warning
                    statusMessage.contains("active") -> LocalAppColors.current.EmulationActive
                    else -> LocalAppColors.current.TextSecondary
                }
                Text(statusMessage, style = MaterialTheme.typography.bodyMedium, color = messageColor)
            }

            Spacer(modifier = Modifier.height(16.dp))

            OutlinedButton(
                onClick = onWriteToTag,
                colors = ButtonDefaults.outlinedButtonColors(contentColor = LocalAppColors.current.Accent),
                border = ButtonDefaults.outlinedButtonBorder(enabled = true).copy(
                    brush = androidx.compose.ui.graphics.SolidColor(LocalAppColors.current.Accent)
                ),
                shape = RoundedCornerShape(NfcDimensions.CornerRadius),
                modifier = Modifier
                    .fillMaxWidth(0.7f)
                    .height(48.dp)
            ) {
                Text(stringResource(R.string.write_to_magic_tag), style = MaterialTheme.typography.titleMedium)
            }

            if (writeProgress.isNotEmpty()) {
                Spacer(modifier = Modifier.height(8.dp))
                val wpColor = when {
                    writeProgress.startsWith("Error") || writeProgress.startsWith("Cannot") || writeProgress.startsWith("Failed") -> LocalAppColors.current.Error
                    writeProgress.contains("complete") || writeProgress.contains("Complete") -> LocalAppColors.current.Secondary
                    writeProgress.contains("Waiting") -> LocalAppColors.current.Warning
                    else -> LocalAppColors.current.Accent
                }
                Text(writeProgress, style = MaterialTheme.typography.bodySmall, color = wpColor)
            }
        } else {
            Text(stringResource(R.string.no_tag_selected), style = MaterialTheme.typography.titleMedium, color = LocalAppColors.current.TextSecondary)
            Spacer(modifier = Modifier.height(16.dp))
            OutlinedButton(
                onClick = onSelectTag,
                colors = ButtonDefaults.outlinedButtonColors(contentColor = LocalAppColors.current.Primary),
                border = ButtonDefaults.outlinedButtonBorder(enabled = true).copy(
                    brush = androidx.compose.ui.graphics.SolidColor(LocalAppColors.current.Primary)
                )
            ) {
                Text(stringResource(R.string.select_a_tag))
            }
        }

        Spacer(modifier = Modifier.weight(1f))
    }
}
