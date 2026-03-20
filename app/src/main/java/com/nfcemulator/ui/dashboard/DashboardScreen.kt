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
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.nfcemulator.R
import com.nfcemulator.ui.theme.LocalAppColors
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
            .background(LocalAppColors.current.Background)
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
                Text(stringResource(R.string.app_name), style = MaterialTheme.typography.headlineLarge, color = LocalAppColors.current.Primary)
                Text("Read. Clone. Emulate.", style = MaterialTheme.typography.bodySmall, color = LocalAppColors.current.Secondary)
            }
            IconButton(onClick = onSettings) {
                Icon(Icons.Default.Settings, contentDescription = "Settings", tint = LocalAppColors.current.TextSecondary)
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
                label = stringResource(R.string.total_tags),
                color = LocalAppColors.current.Primary
            )
            KpiCard(
                modifier = Modifier.weight(1f),
                value = if (stats.totalKeys > 1000) "${stats.totalKeys / 1000}K+" else stats.totalKeys.toString(),
                label = stringResource(R.string.total_keys),
                color = LocalAppColors.current.Secondary
            )
            KpiCard(
                modifier = Modifier.weight(1f),
                value = stats.storageUsed,
                label = stringResource(R.string.storage),
                color = LocalAppColors.current.Accent
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
                    color = LocalAppColors.current.SurfaceVariant,
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
                                .background(if (stats.hasRoot) LocalAppColors.current.Secondary else LocalAppColors.current.Warning)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            stats.emulationMode,
                            style = MaterialTheme.typography.labelMedium,
                            color = LocalAppColors.current.TextSecondary
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(28.dp))

        // Quick Actions
        Text(stringResource(R.string.quick_actions), style = MaterialTheme.typography.titleMedium, color = LocalAppColors.current.TextPrimary)

        Spacer(modifier = Modifier.height(12.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            QuickActionCard(
                modifier = Modifier.weight(1f),
                icon = Icons.Default.Search,
                label = stringResource(R.string.read_tag),
                color = LocalAppColors.current.Primary,
                onClick = onReadTag
            )
            QuickActionCard(
                modifier = Modifier.weight(1f),
                icon = Icons.Default.Add,
                label = stringResource(R.string.import_file),
                color = LocalAppColors.current.Secondary,
                onClick = onImportFile
            )
            QuickActionCard(
                modifier = Modifier.weight(1f),
                icon = Icons.Outlined.Edit,
                label = stringResource(R.string.write_card),
                color = LocalAppColors.current.Accent,
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
            Text(stringResource(R.string.my_tags), style = MaterialTheme.typography.titleMedium, color = LocalAppColors.current.TextPrimary)
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text("View all", style = MaterialTheme.typography.bodySmall, color = LocalAppColors.current.Primary)
                Spacer(modifier = Modifier.width(4.dp))
                Icon(
                    Icons.Default.KeyboardArrowRight,
                    contentDescription = null,
                    tint = LocalAppColors.current.Primary,
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
                    .background(LocalAppColors.current.Surface, RoundedCornerShape(NfcDimensions.CornerRadius))
                    .border(NfcDimensions.BorderWidth, LocalAppColors.current.Border, RoundedCornerShape(NfcDimensions.CornerRadius)),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(stringResource(R.string.no_tags_saved), style = MaterialTheme.typography.bodyMedium, color = LocalAppColors.current.TextSecondary)
                    Text(stringResource(R.string.no_tags_hint), style = MaterialTheme.typography.bodySmall, color = LocalAppColors.current.TextSecondary)
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
            .background(LocalAppColors.current.Surface, RoundedCornerShape(NfcDimensions.CornerRadius))
            .border(NfcDimensions.BorderWidth, LocalAppColors.current.Border, RoundedCornerShape(NfcDimensions.CornerRadius))
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(value, style = MaterialTheme.typography.headlineSmall, color = color)
        Spacer(modifier = Modifier.height(4.dp))
        Text(label, style = MaterialTheme.typography.labelSmall, color = LocalAppColors.current.TextSecondary)
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
            .background(LocalAppColors.current.Surface)
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
        Text(label, style = MaterialTheme.typography.labelMedium, color = LocalAppColors.current.TextPrimary)
    }
}
