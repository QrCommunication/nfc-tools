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
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
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
    val color = LocalAppColors.current

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(color.Background)
            .verticalScroll(rememberScrollState())
    ) {
        // Header area with SurfaceContainer gradient-like background
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(color.SurfaceContainer)
                .padding(horizontal = NfcDimensions.Padding, vertical = 20.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        stringResource(R.string.app_name),
                        style = MaterialTheme.typography.headlineLarge,
                        color = color.Primary,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        stringResource(R.string.tagline),
                        style = MaterialTheme.typography.bodySmall,
                        color = color.Secondary
                    )
                }
                IconButton(onClick = onSettings) {
                    Icon(
                        Icons.Default.Settings,
                        contentDescription = "Settings",
                        tint = color.TextSecondary
                    )
                }
            }
        }

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(NfcDimensions.Padding)
        ) {
            Spacer(modifier = Modifier.height(20.dp))

            // KPI cards row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                KpiCard(
                    modifier = Modifier.weight(1f),
                    value = stats.totalTags.toString(),
                    label = stringResource(R.string.total_tags),
                    icon = Icons.Default.List,
                    color = color.Primary
                )
                KpiCard(
                    modifier = Modifier.weight(1f),
                    value = if (stats.totalKeys > 1000) "${stats.totalKeys / 1000}K+" else stats.totalKeys.toString(),
                    label = stringResource(R.string.total_keys),
                    icon = Icons.Default.Lock,
                    color = color.Secondary
                )
                KpiCard(
                    modifier = Modifier.weight(1f),
                    value = stats.storageUsed,
                    label = stringResource(R.string.storage),
                    icon = Icons.Default.Info,
                    color = color.Accent
                )
            }

            // Root status badge
            if (stats.emulationMode.isNotEmpty()) {
                Spacer(modifier = Modifier.height(14.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.Center
                ) {
                    SuggestionChip(
                        onClick = {},
                        label = {
                            Text(
                                stats.emulationMode,
                                style = MaterialTheme.typography.labelMedium,
                                color = color.TextSecondary
                            )
                        },
                        icon = {
                            Box(
                                modifier = Modifier
                                    .size(8.dp)
                                    .clip(CircleShape)
                                    .background(
                                        if (stats.hasRoot) color.RootActive else color.Warning
                                    )
                            )
                        },
                        colors = SuggestionChipDefaults.suggestionChipColors(
                            containerColor = color.SurfaceContainerHigh,
                            labelColor = color.TextSecondary
                        ),
                        border = SuggestionChipDefaults.suggestionChipBorder(
                            enabled = true,
                            borderColor = color.Border,
                            borderWidth = NfcDimensions.BorderWidth
                        ),
                        shape = RoundedCornerShape(20.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(28.dp))

            // Quick Actions section title
            Text(
                stringResource(R.string.quick_actions).uppercase(),
                style = MaterialTheme.typography.labelLarge,
                color = color.Primary,
                fontWeight = FontWeight.Bold
            )

            Spacer(modifier = Modifier.height(12.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                QuickActionCard(
                    modifier = Modifier.weight(1f),
                    icon = Icons.Default.Search,
                    label = stringResource(R.string.read_tag),
                    color = color.Primary,
                    onClick = onReadTag
                )
                QuickActionCard(
                    modifier = Modifier.weight(1f),
                    icon = Icons.Default.Add,
                    label = stringResource(R.string.import_file),
                    color = color.Secondary,
                    onClick = onImportFile
                )
                QuickActionCard(
                    modifier = Modifier.weight(1f),
                    icon = Icons.Outlined.Edit,
                    label = stringResource(R.string.write_card),
                    color = color.Accent,
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
                Text(
                    stringResource(R.string.my_tags).uppercase(),
                    style = MaterialTheme.typography.labelLarge,
                    color = color.Primary,
                    fontWeight = FontWeight.Bold
                )
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        stringResource(R.string.view_all),
                        style = MaterialTheme.typography.bodySmall,
                        color = color.Primary
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Icon(
                        Icons.Default.KeyboardArrowRight,
                        contentDescription = null,
                        tint = color.Primary,
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
                        .background(color.Surface, RoundedCornerShape(NfcDimensions.CornerRadius))
                        .border(NfcDimensions.BorderWidth, color.Border, RoundedCornerShape(NfcDimensions.CornerRadius)),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            stringResource(R.string.no_tags_saved),
                            style = MaterialTheme.typography.bodyMedium,
                            color = color.TextSecondary
                        )
                        Text(
                            stringResource(R.string.no_tags_hint),
                            style = MaterialTheme.typography.bodySmall,
                            color = color.TextSecondary
                        )
                    }
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
    icon: ImageVector,
    color: androidx.compose.ui.graphics.Color
) {
    val appColors = LocalAppColors.current
    ElevatedCard(
        modifier = modifier,
        shape = RoundedCornerShape(NfcDimensions.CornerRadius),
        colors = CardDefaults.elevatedCardColors(
            containerColor = appColors.SurfaceContainer
        ),
        elevation = CardDefaults.elevatedCardElevation(defaultElevation = 2.dp)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .border(
                    width = 2.dp,
                    color = color.copy(alpha = 0.6f),
                    shape = RoundedCornerShape(
                        topStart = NfcDimensions.CornerRadius,
                        bottomStart = NfcDimensions.CornerRadius,
                        topEnd = 0.dp,
                        bottomEnd = 0.dp
                    )
                )
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 12.dp, vertical = 14.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Box(
                    modifier = Modifier
                        .size(32.dp)
                        .clip(CircleShape)
                        .background(color.copy(alpha = 0.18f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        icon,
                        contentDescription = null,
                        tint = color,
                        modifier = Modifier.size(16.dp)
                    )
                }
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    value,
                    style = MaterialTheme.typography.headlineSmall,
                    color = color,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    label,
                    style = MaterialTheme.typography.labelSmall,
                    color = appColors.TextSecondary
                )
            }
        }
    }
}

@Composable
private fun QuickActionCard(
    modifier: Modifier,
    icon: ImageVector,
    label: String,
    color: androidx.compose.ui.graphics.Color,
    onClick: () -> Unit
) {
    val appColors = LocalAppColors.current
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(16.dp))
            .background(color.copy(alpha = 0.15f))
            .border(NfcDimensions.BorderWidth, color.copy(alpha = 0.35f), RoundedCornerShape(16.dp))
            .clickable(onClick = onClick)
            .padding(vertical = 16.dp, horizontal = 8.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .clip(CircleShape)
                    .background(color.copy(alpha = 0.25f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    icon,
                    contentDescription = label,
                    tint = color,
                    modifier = Modifier.size(28.dp)
                )
            }
            Spacer(modifier = Modifier.height(10.dp))
            Text(
                label,
                style = MaterialTheme.typography.labelMedium,
                color = appColors.TextPrimary,
                fontWeight = FontWeight.Medium
            )
        }
    }
}
