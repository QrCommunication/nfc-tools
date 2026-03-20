package com.nfcemulator.ui.settings

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.nfcemulator.R
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
    currentLanguage: String,
    onSetLanguage: (String) -> Unit,
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
        Text(stringResource(R.string.settings_title), style = MaterialTheme.typography.headlineLarge, color = LocalAppColors.current.Primary)

        Spacer(modifier = Modifier.height(24.dp))

        SettingsSection(stringResource(R.string.device_status)) {
            SettingsRow(stringResource(R.string.root_access), if (hasRoot) stringResource(R.string.available) else stringResource(R.string.not_available),
                valueColor = if (hasRoot) LocalAppColors.current.Secondary else LocalAppColors.current.Warning)
            SettingsRow(stringResource(R.string.nxp_chipset), if (hasNxpChipset) stringResource(R.string.detected) else stringResource(R.string.not_detected),
                valueColor = if (hasNxpChipset) LocalAppColors.current.Secondary else LocalAppColors.current.TextSecondary)
            SettingsRow(stringResource(R.string.emulation_mode), emulationMode,
                valueColor = if (emulationMode.contains("Full")) LocalAppColors.current.RootActive else LocalAppColors.current.HceOnly)
        }

        Spacer(modifier = Modifier.height(16.dp))

        SettingsSection(stringResource(R.string.dictionaries)) {
            SettingsRow(stringResource(R.string.total_keys), "$totalKeys keys loaded")
        }

        Spacer(modifier = Modifier.height(16.dp))

        SettingsSection(stringResource(R.string.storage)) {
            SettingsRow(stringResource(R.string.saved_tags), "$totalTags tags")
            SettingsRow(stringResource(R.string.storage_used), storageSize)
        }

        Spacer(modifier = Modifier.height(16.dp))

        SettingsSection(stringResource(R.string.appearance)) {
            Row(
                modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(stringResource(R.string.dark_mode), style = MaterialTheme.typography.bodyMedium, color = LocalAppColors.current.TextSecondary)
                Switch(
                    checked = isDarkMode,
                    onCheckedChange = { onToggleDarkMode() },
                    colors = SwitchDefaults.colors(
                        checkedThumbColor = LocalAppColors.current.Primary,
                        checkedTrackColor = LocalAppColors.current.Primary.copy(alpha = 0.3f)
                    )
                )
            }
            Spacer(modifier = Modifier.height(8.dp))
            Row(
                modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(stringResource(R.string.language), style = MaterialTheme.typography.bodyMedium, color = LocalAppColors.current.TextSecondary)
                Row {
                    listOf("EN" to "en", "FR" to "fr").forEach { (label, code) ->
                        val isSelected = currentLanguage == code
                        Surface(
                            color = if (isSelected) LocalAppColors.current.Primary else LocalAppColors.current.SurfaceVariant,
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier
                                .padding(horizontal = 4.dp)
                                .clickable { onSetLanguage(code) }
                        ) {
                            Text(
                                label,
                                style = MaterialTheme.typography.labelMedium,
                                color = if (isSelected) LocalAppColors.current.Background else LocalAppColors.current.TextSecondary,
                                modifier = Modifier.padding(horizontal = 14.dp, vertical = 6.dp)
                            )
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        SettingsSection(stringResource(R.string.backup)) {
            Button(
                onClick = onExportBackup,
                colors = ButtonDefaults.buttonColors(containerColor = LocalAppColors.current.Primary),
                shape = RoundedCornerShape(NfcDimensions.CornerRadius),
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(stringResource(R.string.export_backup), color = LocalAppColors.current.Background)
            }
            Spacer(modifier = Modifier.height(8.dp))
            OutlinedButton(
                onClick = onImportBackup,
                colors = ButtonDefaults.outlinedButtonColors(contentColor = LocalAppColors.current.Primary),
                border = ButtonDefaults.outlinedButtonBorder.copy(brush = androidx.compose.ui.graphics.SolidColor(LocalAppColors.current.Primary)),
                shape = RoundedCornerShape(NfcDimensions.CornerRadius),
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(stringResource(R.string.import_backup))
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
