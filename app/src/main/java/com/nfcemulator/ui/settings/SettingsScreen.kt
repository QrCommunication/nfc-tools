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
import com.nfcemulator.ui.theme.LocalAppColors
import com.nfcemulator.ui.theme.NfcDimensions

@Composable
fun SettingsScreen(
    hasRoot: Boolean,
    hasNxpChipset: Boolean,
    emulationMode: String,
    totalKeys: Int,
    totalTags: Int,
    storageSize: String,
    isDarkMode: Boolean,
    onToggleDarkMode: () -> Unit,
    onExportBackup: () -> Unit,
    onImportBackup: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(LocalAppColors.current.Background)
            .padding(NfcDimensions.Padding)
            .verticalScroll(rememberScrollState())
    ) {
        Text("Settings", style = MaterialTheme.typography.headlineLarge, color = LocalAppColors.current.Primary)

        Spacer(modifier = Modifier.height(24.dp))

        SettingsSection("Device Status") {
            SettingsRow("Root Access", if (hasRoot) "Available" else "Not available",
                valueColor = if (hasRoot) LocalAppColors.current.Secondary else LocalAppColors.current.Warning)
            SettingsRow("NXP Chipset", if (hasNxpChipset) "Detected" else "Not detected",
                valueColor = if (hasNxpChipset) LocalAppColors.current.Secondary else LocalAppColors.current.TextSecondary)
            SettingsRow("Emulation Mode", emulationMode,
                valueColor = if (emulationMode.contains("Full")) LocalAppColors.current.RootActive else LocalAppColors.current.HceOnly)
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

        SettingsSection("Appearance") {
            Row(
                modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("Dark Mode", style = MaterialTheme.typography.bodyMedium, color = LocalAppColors.current.TextSecondary)
                Switch(
                    checked = isDarkMode,
                    onCheckedChange = { onToggleDarkMode() },
                    colors = SwitchDefaults.colors(
                        checkedThumbColor = LocalAppColors.current.Primary,
                        checkedTrackColor = LocalAppColors.current.Primary.copy(alpha = 0.3f)
                    )
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        SettingsSection("Backup") {
            Button(
                onClick = onExportBackup,
                colors = ButtonDefaults.buttonColors(containerColor = LocalAppColors.current.Primary),
                shape = RoundedCornerShape(NfcDimensions.CornerRadius),
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Export Backup", color = LocalAppColors.current.Background)
            }
            Spacer(modifier = Modifier.height(8.dp))
            OutlinedButton(
                onClick = onImportBackup,
                colors = ButtonDefaults.outlinedButtonColors(contentColor = LocalAppColors.current.Primary),
                border = ButtonDefaults.outlinedButtonBorder.copy(brush = androidx.compose.ui.graphics.SolidColor(LocalAppColors.current.Primary)),
                shape = RoundedCornerShape(NfcDimensions.CornerRadius),
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Import Backup")
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        Text(
            "NFC Emulator v1.0.0 — GPL v3",
            style = MaterialTheme.typography.bodySmall,
            color = LocalAppColors.current.TextSecondary
        )
    }
}

@Composable
private fun SettingsSection(title: String, content: @Composable ColumnScope.() -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(LocalAppColors.current.Surface, RoundedCornerShape(NfcDimensions.CornerRadius))
            .padding(NfcDimensions.CardPadding)
    ) {
        Text(title, style = MaterialTheme.typography.titleMedium, color = LocalAppColors.current.Primary)
        Spacer(modifier = Modifier.height(12.dp))
        content()
    }
}

@Composable
private fun SettingsRow(label: String, value: String, valueColor: androidx.compose.ui.graphics.Color = LocalAppColors.current.TextPrimary) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(label, style = MaterialTheme.typography.bodyMedium, color = LocalAppColors.current.TextSecondary)
        Text(value, style = MaterialTheme.typography.bodyMedium, color = valueColor)
    }
}
