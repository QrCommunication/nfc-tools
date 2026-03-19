package com.nfcemulator.ui.editor

import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.nfcemulator.dump.model.Sector
import com.nfcemulator.dump.model.TagDump
import com.nfcemulator.ui.theme.NfcColors
import com.nfcemulator.ui.theme.NfcDimensions
import com.nfcemulator.ui.theme.NfcMonoStyles

@Composable
fun EditorScreen(dump: TagDump?) {
    if (dump == null) {
        Box(modifier = Modifier.fillMaxSize().background(NfcColors.Background), contentAlignment = androidx.compose.ui.Alignment.Center) {
            Text("No dump loaded", style = MaterialTheme.typography.titleMedium, color = NfcColors.TextSecondary)
        }
        return
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(NfcColors.Background)
            .padding(NfcDimensions.Padding)
    ) {
        Text("Hex Editor", style = MaterialTheme.typography.headlineLarge, color = NfcColors.Primary)

        Spacer(modifier = Modifier.height(8.dp))

        Row {
            Text("UID: ", style = MaterialTheme.typography.bodyMedium, color = NfcColors.TextSecondary)
            Text(dump.uidHex, style = NfcMonoStyles.uid)
            Spacer(modifier = Modifier.width(16.dp))
            Text("Type: ", style = MaterialTheme.typography.bodyMedium, color = NfcColors.TextSecondary)
            Text(dump.type.displayName, style = MaterialTheme.typography.bodyMedium, color = NfcColors.TextPrimary)
        }

        Spacer(modifier = Modifier.height(4.dp))

        Text(
            "Keys: ${dump.foundKeys}/${dump.totalKeys}",
            style = MaterialTheme.typography.bodySmall,
            color = if (dump.foundKeys == dump.totalKeys) NfcColors.Secondary else NfcColors.Warning
        )

        Spacer(modifier = Modifier.height(16.dp))

        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
        ) {
            dump.sectors.forEach { sector ->
                SectorView(sector)
                Spacer(modifier = Modifier.height(8.dp))
            }
        }
    }
}

@Composable
private fun SectorView(sector: Sector) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(NfcColors.Surface, RoundedCornerShape(NfcDimensions.CornerRadius))
            .padding(12.dp)
    ) {
        Row {
            Text(
                "Sector ${sector.index}",
                style = MaterialTheme.typography.labelLarge,
                color = NfcColors.Primary
            )
            Spacer(modifier = Modifier.weight(1f))
            val keyStatus = when {
                sector.keyA != null && sector.keyB != null -> "A+B"
                sector.keyA != null -> "A"
                sector.keyB != null -> "B"
                else -> "Locked"
            }
            val keyColor = when {
                sector.keyA != null && sector.keyB != null -> NfcColors.KeyFound
                sector.keyA != null || sector.keyB != null -> NfcColors.Warning
                else -> NfcColors.KeyMissing
            }
            Text(keyStatus, style = MaterialTheme.typography.labelSmall, color = keyColor)
        }

        Spacer(modifier = Modifier.height(6.dp))

        sector.blocks.forEach { block ->
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState())
                    .padding(vertical = 1.dp)
            ) {
                Text(
                    "%03d: ".format(block.index),
                    style = NfcMonoStyles.hexData,
                    color = NfcColors.TextSecondary
                )
                val hexGroups = block.data.toList().chunked(4)
                hexGroups.forEachIndexed { groupIdx, group ->
                    group.forEach { byte ->
                        val color = when {
                            block.isTrailerBlock && groupIdx == 0 -> NfcColors.HexKeyA
                            block.isTrailerBlock && groupIdx == 1 -> NfcColors.HexAccessBits
                            block.isTrailerBlock && (groupIdx == 2 || groupIdx == 3) -> NfcColors.HexKeyB
                            else -> NfcColors.HexDefault
                        }
                        Text("%02X ".format(byte), style = NfcMonoStyles.hexData, color = color)
                    }
                    if (groupIdx < hexGroups.size - 1) {
                        Text(" ", style = NfcMonoStyles.hexData)
                    }
                }
            }
        }

        if (sector.keyA != null) {
            Spacer(modifier = Modifier.height(4.dp))
            Text("Key A: ${sector.keyA!!.joinToString(":") { "%02X".format(it) }}", style = NfcMonoStyles.key, color = NfcColors.HexKeyA)
        }
        if (sector.keyB != null) {
            Text("Key B: ${sector.keyB!!.joinToString(":") { "%02X".format(it) }}", style = NfcMonoStyles.key, color = NfcColors.HexKeyB)
        }
    }
}
