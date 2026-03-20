package com.nfcemulator.ui.dashboard

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.Edit
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import com.nfcemulator.ui.theme.NfcColors
import com.nfcemulator.ui.theme.NfcDimensions

data class DashboardStats(
    val totalTags: Int = 0,
    val totalKeys: Int = 0,
    val storageUsed: String = "0 KB",
    val hasRoot: Boolean = false,
    val emulationMode: String = ""
)

@Composable
fun DashboardScreen(
    stats: DashboardStats,
    onReadTag: () -> Unit,
    onImportFile: () -> Unit,
    onWriteCard: () -> Unit,
    onMyTags: () -> Unit,
    onSettings: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(NfcColors.Background)
            .verticalScroll(rememberScrollState())
            .padding(NfcDimensions.Padding)
    ) {
        // Header
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text("NFC Emulator", style = MaterialTheme.typography.headlineLarge, color = NfcColors.Primary)
                Text("Read. Clone. Emulate.", style = MaterialTheme.typography.bodySmall, color = NfcColors.Secondary)
            }
            IconButton(onClick = onSettings) {
                Icon(Icons.Default.Settings, contentDescription = "Settings", tint = NfcColors.TextSecondary)
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        // KPI cards row
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            KpiCard(
                modifier = Modifier.weight(1f),
                value = stats.totalTags.toString(),
                label = "Tags",
                color = NfcColors.Primary
            )
            KpiCard(
                modifier = Modifier.weight(1f),
                value = if (stats.totalKeys > 1000) "${stats.totalKeys / 1000}K+" else stats.totalKeys.toString(),
                label = "Keys",
                color = NfcColors.Secondary
            )
            KpiCard(
                modifier = Modifier.weight(1f),
                value = stats.storageUsed,
                label = "Storage",
                color = NfcColors.Accent
            )
        }

        // Root status badge
        if (stats.emulationMode.isNotEmpty()) {
            Spacer(modifier = Modifier.height(12.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Center
            ) {
                Surface(
                    color = NfcColors.SurfaceVariant,
                    shape = RoundedCornerShape(20.dp)
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(8.dp)
                                .clip(CircleShape)
                                .background(if (stats.hasRoot) NfcColors.Secondary else NfcColors.Warning)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            stats.emulationMode,
                            style = MaterialTheme.typography.labelMedium,
                            color = NfcColors.TextSecondary
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(28.dp))

        // Quick Actions
        Text("Quick Actions", style = MaterialTheme.typography.titleMedium, color = NfcColors.TextPrimary)

        Spacer(modifier = Modifier.height(12.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            QuickActionCard(
                modifier = Modifier.weight(1f),
                icon = Icons.Default.Search,
                label = "Read Tag",
                color = NfcColors.Primary,
                onClick = onReadTag
            )
            QuickActionCard(
                modifier = Modifier.weight(1f),
                icon = Icons.Default.Add,
                label = "Import",
                color = NfcColors.Secondary,
                onClick = onImportFile
            )
            QuickActionCard(
                modifier = Modifier.weight(1f),
                icon = Icons.Outlined.Edit,
                label = "Write",
                color = NfcColors.Accent,
                onClick = onWriteCard
            )
        }

        Spacer(modifier = Modifier.height(28.dp))

        // My Tags section header
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clickable(onClick = onMyTags),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text("My Tags", style = MaterialTheme.typography.titleMedium, color = NfcColors.TextPrimary)
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text("View all", style = MaterialTheme.typography.bodySmall, color = NfcColors.Primary)
                Spacer(modifier = Modifier.width(4.dp))
                Icon(
                    Icons.Default.KeyboardArrowRight,
                    contentDescription = null,
                    tint = NfcColors.Primary,
                    modifier = Modifier.size(16.dp)
                )
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        if (stats.totalTags == 0) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(100.dp)
                    .background(NfcColors.Surface, RoundedCornerShape(NfcDimensions.CornerRadius))
                    .border(NfcDimensions.BorderWidth, NfcColors.Border, RoundedCornerShape(NfcDimensions.CornerRadius)),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("No tags yet", style = MaterialTheme.typography.bodyMedium, color = NfcColors.TextSecondary)
                    Text("Read a tag to get started", style = MaterialTheme.typography.bodySmall, color = NfcColors.TextSecondary)
                }
            }
        }
    }
}

@Composable
private fun KpiCard(
    modifier: Modifier,
    value: String,
    label: String,
    color: androidx.compose.ui.graphics.Color
) {
    Column(
        modifier = modifier
            .background(NfcColors.Surface, RoundedCornerShape(NfcDimensions.CornerRadius))
            .border(NfcDimensions.BorderWidth, NfcColors.Border, RoundedCornerShape(NfcDimensions.CornerRadius))
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(value, style = MaterialTheme.typography.headlineSmall, color = color)
        Spacer(modifier = Modifier.height(4.dp))
        Text(label, style = MaterialTheme.typography.labelSmall, color = NfcColors.TextSecondary)
    }
}

@Composable
private fun QuickActionCard(
    modifier: Modifier,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    label: String,
    color: androidx.compose.ui.graphics.Color,
    onClick: () -> Unit
) {
    Column(
        modifier = modifier
            .clip(RoundedCornerShape(NfcDimensions.CornerRadius))
            .background(NfcColors.Surface)
            .border(NfcDimensions.BorderWidth, color.copy(alpha = 0.3f), RoundedCornerShape(NfcDimensions.CornerRadius))
            .clickable(onClick = onClick)
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Box(
            modifier = Modifier
                .size(44.dp)
                .clip(CircleShape)
                .background(color.copy(alpha = 0.15f)),
            contentAlignment = Alignment.Center
        ) {
            Icon(icon, contentDescription = label, tint = color, modifier = Modifier.size(22.dp))
        }
        Spacer(modifier = Modifier.height(8.dp))
        Text(label, style = MaterialTheme.typography.labelMedium, color = NfcColors.TextPrimary)
    }
}
