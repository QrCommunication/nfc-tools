package com.nfcemulator.ui.editor

import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.nfcemulator.R
import com.nfcemulator.dump.model.Sector
import com.nfcemulator.dump.model.TagDump
import com.nfcemulator.ui.theme.LocalAppColors
import com.nfcemulator.ui.theme.NfcDimensions
import com.nfcemulator.ui.theme.NfcMonoStyles

@Composable
fun EditorScreen(dump: TagDump?) {
    if (dump == null) {
        Box(modifier = Modifier.fillMaxSize().background(LocalAppColors.current.Background), contentAlignment = Alignment.Center) {
            Text(stringResource(R.string.no_dump_loaded), style = MaterialTheme.typography.titleMedium, color = LocalAppColors.current.TextSecondary)
        }
        return
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(LocalAppColors.current.Background)
            .padding(NfcDimensions.Padding)
    ) {
        Text(stringResource(R.string.hex_editor), style = MaterialTheme.typography.headlineLarge, color = LocalAppColors.current.Primary)

        Spacer(modifier = Modifier.height(12.dp))

        // Header info card
        Surface(
            color = LocalAppColors.current.SurfaceContainerHigh,
            shape = RoundedCornerShape(NfcDimensions.CornerRadius),
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 10.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text("UID", style = MaterialTheme.typography.labelSmall, color = LocalAppColors.current.TextSecondary)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(dump.uidHex, style = NfcMonoStyles.uid, color = LocalAppColors.current.Accent)
                    }
                    Spacer(modifier = Modifier.height(4.dp))
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text("Type", style = MaterialTheme.typography.labelSmall, color = LocalAppColors.current.TextSecondary)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(dump.type.displayName, style = MaterialTheme.typography.bodyMedium, color = LocalAppColors.current.TextPrimary)
                    }
                }
                Surface(
                    color = if (dump.foundKeys == dump.totalKeys) LocalAppColors.current.KeyFound.copy(alpha = 0.15f)
                            else LocalAppColors.current.Warning.copy(alpha = 0.15f),
                    shape = RoundedCornerShape(NfcDimensions.CornerRadiusExtraLarge)
                ) {
                    Text(
                        "Keys ${dump.foundKeys}/${dump.totalKeys}",
                        style = MaterialTheme.typography.labelMedium,
                        color = if (dump.foundKeys == dump.totalKeys) LocalAppColors.current.KeyFound else LocalAppColors.current.Warning,
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                    )
                }
            }
        }

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
    val colors = LocalAppColors.current
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(NfcDimensions.CornerRadius))
            .background(colors.SurfaceContainer)
    ) {
        // Colored left accent bar
        Box(
            modifier = Modifier
                .width(4.dp)
                .fillMaxHeight()
                .background(
                    when {
                        sector.keyA != null && sector.keyB != null -> colors.KeyFound
                        sector.keyA != null || sector.keyB != null -> colors.Warning
                        else -> colors.KeyMissing
                    }
                )
        )

        Column(modifier = Modifier.weight(1f).padding(12.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    "Sector ${sector.index}",
                    style = MaterialTheme.typography.labelLarge,
                    color = colors.Primary
                )
                Spacer(modifier = Modifier.weight(1f))
                val keyStatus = when {
                    sector.keyA != null && sector.keyB != null -> "A+B"
                    sector.keyA != null -> "A"
                    sector.keyB != null -> "B"
                    else -> stringResource(R.string.locked)
                }
                val keyColor = when {
                    sector.keyA != null && sector.keyB != null -> colors.KeyFound
                    sector.keyA != null || sector.keyB != null -> colors.Warning
                    else -> colors.KeyMissing
                }
                Surface(
                    color = keyColor.copy(alpha = 0.15f),
                    shape = RoundedCornerShape(NfcDimensions.CornerRadiusExtraLarge)
                ) {
                    Text(
                        keyStatus,
                        style = MaterialTheme.typography.labelSmall,
                        color = keyColor,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(6.dp))

            sector.blocks.forEachIndexed { idx, block ->
                val rowBg = if (idx % 2 == 0) colors.Surface else colors.SurfaceContainer
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(rowBg, RoundedCornerShape(NfcDimensions.CornerRadiusExtraSmall))
                        .horizontalScroll(rememberScrollState())
                        .padding(vertical = 2.dp, horizontal = 4.dp)
                ) {
                    Text(
                        "%03d: ".format(block.index),
                        style = NfcMonoStyles.hexData,
                        color = colors.TextSecondary
                    )
                    val hexGroups = block.data.toList().chunked(4)
                    hexGroups.forEachIndexed { groupIdx, group ->
                        group.forEach { byte ->
                            val color = when {
                                block.isTrailerBlock && groupIdx == 0 -> colors.HexKeyA
                                block.isTrailerBlock && groupIdx == 1 -> colors.HexAccessBits
                                block.isTrailerBlock && (groupIdx == 2 || groupIdx == 3) -> colors.HexKeyB
                                else -> colors.HexDefault
                            }
                            Text("%02X ".format(byte), style = NfcMonoStyles.hexData, color = color)
                        }
                        if (groupIdx < hexGroups.size - 1) {
                            Text(" ", style = NfcMonoStyles.hexData, color = colors.TextSecondary)
                        }
                    }
                }
            }

            if (sector.keyA != null || sector.keyB != null) {
                Spacer(modifier = Modifier.height(8.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    if (sector.keyA != null) {
                        Surface(
                            color = colors.HexKeyA.copy(alpha = 0.15f),
                            shape = RoundedCornerShape(NfcDimensions.CornerRadiusSmall)
                        ) {
                            Text(
                                "A: ${sector.keyA!!.joinToString(":") { "%02X".format(it) }}",
                                style = NfcMonoStyles.key,
                                color = colors.HexKeyA,
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                            )
                        }
                    }
                    if (sector.keyB != null) {
                        Surface(
                            color = colors.HexKeyB.copy(alpha = 0.15f),
                            shape = RoundedCornerShape(NfcDimensions.CornerRadiusSmall)
                        ) {
                            Text(
                                "B: ${sector.keyB!!.joinToString(":") { "%02X".format(it) }}",
                                style = NfcMonoStyles.key,
                                color = colors.HexKeyB,
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                            )
                        }
                    }
                }
            }
        }
    }
}
