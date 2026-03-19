package com.nfcemulator.ui.settings

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.nfcemulator.ui.theme.NfcColors
import com.nfcemulator.ui.theme.NfcDimensions

@Composable
fun SettingsScreen(
    hasRoot: Boolean,
    hasNxpChipset: Boolean,
    emulationMode: String,
    totalKeys: Int,
    totalTags: Int,
    storageSize: String,
    nxpDebugLog: String = "",
    onExportBackup: () -> Unit,
    onImportBackup: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(NfcColors.Background)
            .padding(NfcDimensions.Padding)
            .verticalScroll(rememberScrollState())
    ) {
        Text("Settings", style = MaterialTheme.typography.headlineLarge, color = NfcColors.Primary)

        Spacer(modifier = Modifier.height(24.dp))

        SettingsSection("Device Status") {
            SettingsRow("Root Access", if (hasRoot) "Available" else "Not available",
                valueColor = if (hasRoot) NfcColors.Secondary else NfcColors.Warning)
            SettingsRow("NXP Chipset", if (hasNxpChipset) "Detected" else "Not detected",
                valueColor = if (hasNxpChipset) NfcColors.Secondary else NfcColors.TextSecondary)
            SettingsRow("Emulation Mode", emulationMode,
                valueColor = if (emulationMode.contains("Full")) NfcColors.RootActive else NfcColors.HceOnly)
        }

        Spacer(modifier = Modifier.height(16.dp))

        SettingsSection("Dictionaries") {
            SettingsRow("Total Keys", "$totalKeys keys loaded")
        }

        Spacer(modifier = Modifier.height(16.dp))

        SettingsSection("Storage") {
            SettingsRow("Saved Tags", "$totalTags tags")
            SettingsRow("Storage Used", storageSize)
        }

        Spacer(modifier = Modifier.height(16.dp))

        SettingsSection("Backup") {
            Button(
                onClick = onExportBackup,
                colors = ButtonDefaults.buttonColors(containerColor = NfcColors.Primary),
                shape = RoundedCornerShape(NfcDimensions.CornerRadius),
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Export Backup", color = NfcColors.Background)
            }
            Spacer(modifier = Modifier.height(8.dp))
            OutlinedButton(
                onClick = onImportBackup,
                colors = ButtonDefaults.outlinedButtonColors(contentColor = NfcColors.Primary),
                border = ButtonDefaults.outlinedButtonBorder.copy(brush = androidx.compose.ui.graphics.SolidColor(NfcColors.Primary)),
                shape = RoundedCornerShape(NfcDimensions.CornerRadius),
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Import Backup")
            }
        }

        if (nxpDebugLog.isNotEmpty()) {
            Spacer(modifier = Modifier.height(16.dp))
            SettingsSection("NXP Detection Log") {
                Text(
                    nxpDebugLog,
                    style = MaterialTheme.typography.bodySmall.copy(
                        fontFamily = androidx.compose.ui.text.font.FontFamily.Monospace,
                        fontSize = androidx.compose.ui.unit.TextUnit(10f, androidx.compose.ui.unit.TextUnitType.Sp)
                    ),
                    color = NfcColors.TextSecondary
                )
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        Text(
            "NFC Emulator v1.0.0 — GPL v3",
            style = MaterialTheme.typography.bodySmall,
            color = NfcColors.TextSecondary
        )
    }
}

@Composable
private fun SettingsSection(title: String, content: @Composable ColumnScope.() -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(NfcColors.Surface, RoundedCornerShape(NfcDimensions.CornerRadius))
            .padding(NfcDimensions.CardPadding)
    ) {
        Text(title, style = MaterialTheme.typography.titleMedium, color = NfcColors.Primary)
        Spacer(modifier = Modifier.height(12.dp))
        content()
    }
}

@Composable
private fun SettingsRow(label: String, value: String, valueColor: androidx.compose.ui.graphics.Color = NfcColors.TextPrimary) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(label, style = MaterialTheme.typography.bodyMedium, color = NfcColors.TextSecondary)
        Text(value, style = MaterialTheme.typography.bodyMedium, color = valueColor)
    }
}
