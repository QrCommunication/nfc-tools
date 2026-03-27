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
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
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
    val colors = LocalAppColors.current

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(colors.Background)
            .padding(NfcDimensions.Padding)
            .verticalScroll(rememberScrollState())
    ) {
        Text(stringResource(R.string.settings_title), style = MaterialTheme.typography.headlineLarge, color = colors.Primary)

        Spacer(modifier = Modifier.height(24.dp))

        SettingsSection(stringResource(R.string.device_status)) {
            SettingsRow(stringResource(R.string.root_access), if (hasRoot) stringResource(R.string.available) else stringResource(R.string.not_available),
                valueColor = if (hasRoot) colors.Secondary else colors.Warning)
            SettingsRow(stringResource(R.string.nxp_chipset), if (hasNxpChipset) stringResource(R.string.detected) else stringResource(R.string.not_detected),
                valueColor = if (hasNxpChipset) colors.Secondary else colors.TextSecondary)
            SettingsRow(stringResource(R.string.emulation_mode), emulationMode,
                valueColor = if (emulationMode.contains("Full")) colors.RootActive else colors.HceOnly)
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
                Text(stringResource(R.string.dark_mode), style = MaterialTheme.typography.bodyMedium, color = colors.TextSecondary)
                Switch(
                    checked = isDarkMode,
                    onCheckedChange = { onToggleDarkMode() },
                    colors = SwitchDefaults.colors(
                        checkedThumbColor = colors.Background,
                        checkedTrackColor = colors.Primary,
                        uncheckedThumbColor = colors.TextSecondary,
                        uncheckedTrackColor = colors.SurfaceContainerHigh
                    )
                )
            }
            Spacer(modifier = Modifier.height(8.dp))
            Row(
                modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(stringResource(R.string.language), style = MaterialTheme.typography.bodyMedium, color = colors.TextSecondary)
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    listOf("EN" to "en", "FR" to "fr").forEach { (label, code) ->
                        val isSelected = currentLanguage == code
                        Surface(
                            color = if (isSelected) colors.Primary else colors.SurfaceContainerHigh,
                            shape = RoundedCornerShape(NfcDimensions.CornerRadiusExtraLarge),
                            modifier = Modifier.clickable { onSetLanguage(code) }
                        ) {
                            Text(
                                label,
                                style = MaterialTheme.typography.labelMedium,
                                color = if (isSelected) colors.Background else colors.TextSecondary,
                                modifier = Modifier.padding(horizontal = 18.dp, vertical = 8.dp)
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
                colors = ButtonDefaults.buttonColors(containerColor = colors.Primary),
                shape = RoundedCornerShape(28.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(stringResource(R.string.export_backup), color = colors.Background)
            }
            Spacer(modifier = Modifier.height(8.dp))
            OutlinedButton(
                onClick = onImportBackup,
                colors = ButtonDefaults.outlinedButtonColors(contentColor = colors.Primary),
                border = ButtonDefaults.outlinedButtonBorder.copy(brush = SolidColor(colors.Primary)),
                shape = RoundedCornerShape(28.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(stringResource(R.string.import_backup))
            }
        }

        Spacer(modifier = Modifier.height(32.dp))

        Text(
            stringResource(R.string.version_info),
            style = MaterialTheme.typography.bodySmall,
            color = colors.TextSecondary,
            textAlign = TextAlign.Center,
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(16.dp))
    }
}

@Composable
private fun SettingsSection(title: String, content: @Composable ColumnScope.() -> Unit) {
    val colors = LocalAppColors.current
    ElevatedCard(
        colors = CardDefaults.elevatedCardColors(containerColor = colors.SurfaceContainer),
        elevation = CardDefaults.elevatedCardElevation(defaultElevation = 2.dp),
        shape = RoundedCornerShape(NfcDimensions.CornerRadius),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(modifier = Modifier.fillMaxWidth()) {
            Box(
                modifier = Modifier
                    .width(4.dp)
                    .fillMaxHeight()
                    .background(
                        colors.Primary,
                        RoundedCornerShape(topStart = NfcDimensions.CornerRadius, bottomStart = NfcDimensions.CornerRadius)
                    )
            )
            Column(modifier = Modifier.weight(1f).padding(NfcDimensions.CardPadding)) {
                Text(title, style = MaterialTheme.typography.titleMedium, color = colors.Primary)
                Spacer(modifier = Modifier.height(12.dp))
                content()
            }
        }
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
