package com.nfcemulator.ui.writer

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.nfcemulator.R
import com.nfcemulator.ui.home.TagUiModel
import com.nfcemulator.ui.theme.LocalAppColors
import com.nfcemulator.ui.theme.NfcDimensions
import com.nfcemulator.ui.theme.NfcMonoStyles

@Composable
fun WriteScreen(
    tags: List<TagUiModel>,
    selectedTag: TagUiModel?,
    writeProgress: String,
    onSelectTag: (TagUiModel) -> Unit,
    onStartWrite: () -> Unit,
    onCancelWrite: () -> Unit
) {
    val colors = LocalAppColors.current

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(colors.Background)
            .padding(NfcDimensions.Padding),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(stringResource(R.string.write_title), style = MaterialTheme.typography.headlineLarge, color = colors.Accent)

        Spacer(modifier = Modifier.height(4.dp))

        Text(
            stringResource(R.string.write_description),
            style = MaterialTheme.typography.bodySmall,
            color = colors.TextSecondary
        )

        Spacer(modifier = Modifier.height(16.dp))

        if (selectedTag != null) {
            // Selected tag card with SurfaceContainer bg + Accent border + "Selected" chip
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(colors.SurfaceContainer, RoundedCornerShape(NfcDimensions.CornerRadius))
                    .border(NfcDimensions.BorderWidth, colors.Accent, RoundedCornerShape(NfcDimensions.CornerRadius))
            ) {
                // Colored top accent bar
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(4.dp)
                        .background(
                            colors.Accent,
                            RoundedCornerShape(topStart = NfcDimensions.CornerRadius, topEnd = NfcDimensions.CornerRadius)
                        )
                )
                Column(
                    modifier = Modifier.padding(NfcDimensions.CardPadding),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    // "Selected" chip
                    Surface(
                        color = colors.Accent.copy(alpha = 0.15f),
                        shape = RoundedCornerShape(50),
                        modifier = Modifier.wrapContentSize()
                    ) {
                        Text(
                            stringResource(R.string.selected),
                            style = MaterialTheme.typography.labelSmall,
                            color = colors.Accent,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 3.dp)
                        )
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(selectedTag.name, style = MaterialTheme.typography.titleMedium, color = colors.TextPrimary, fontWeight = FontWeight.SemiBold)
                    Text("UID: ${selectedTag.uid}", style = NfcMonoStyles.uid.copy(fontSize = MaterialTheme.typography.bodySmall.fontSize), color = colors.Secondary)
                    Text(selectedTag.type, style = MaterialTheme.typography.bodySmall, color = colors.TextSecondary)
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            val isWaiting = writeProgress.contains("Waiting") || writeProgress.contains("Writing")

            Button(
                onClick = if (isWaiting) onCancelWrite else onStartWrite,
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (isWaiting) colors.Error else colors.Accent,
                    contentColor = colors.Background
                ),
                shape = RoundedCornerShape(28.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp)
            ) {
                Text(
                    if (isWaiting) "Cancel" else stringResource(R.string.write_to_magic_tag),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
            }

            if (writeProgress.isNotEmpty()) {
                Spacer(modifier = Modifier.height(12.dp))
                val statusColor = when {
                    writeProgress.contains("Error") || writeProgress.contains("Cannot") || writeProgress.contains("Failed") -> colors.Error
                    writeProgress.contains("complete") || writeProgress.contains("Complete") -> colors.Secondary
                    writeProgress.contains("Waiting") -> colors.Warning
                    else -> colors.Accent
                }
                // Status message as a pill/badge
                Surface(
                    color = statusColor.copy(alpha = 0.12f),
                    shape = RoundedCornerShape(50),
                    modifier = Modifier.wrapContentSize()
                ) {
                    Text(
                        writeProgress,
                        style = MaterialTheme.typography.bodyMedium,
                        color = statusColor,
                        fontWeight = FontWeight.Medium,
                        modifier = Modifier.padding(horizontal = 14.dp, vertical = 6.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            Text(
                stringResource(R.string.select_another),
                style = MaterialTheme.typography.labelMedium,
                color = colors.TextSecondary,
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(8.dp))
        } else {
            Text(
                stringResource(R.string.select_tag_write),
                style = MaterialTheme.typography.titleMedium,
                color = colors.TextSecondary
            )
            Spacer(modifier = Modifier.height(16.dp))
        }

        LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            items(tags, key = { it.id }) { tag ->
                val isSelected = selectedTag?.id == tag.id
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(NfcDimensions.CornerRadiusSmall))
                        .background(
                            if (isSelected) colors.Accent.copy(alpha = 0.12f) else colors.SurfaceContainer
                        )
                        .border(
                            NfcDimensions.BorderWidth,
                            if (isSelected) colors.Accent else colors.Border,
                            RoundedCornerShape(NfcDimensions.CornerRadiusSmall)
                        )
                        .clickable { onSelectTag(tag) }
                        .padding(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            tag.name,
                            style = MaterialTheme.typography.bodyMedium,
                            color = if (isSelected) colors.Accent else colors.TextPrimary,
                            fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Normal,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        Text("UID: ${tag.uid}", style = MaterialTheme.typography.bodySmall, color = colors.TextSecondary)
                    }
                    Text(
                        tag.type,
                        style = MaterialTheme.typography.labelSmall,
                        color = if (isSelected) colors.Accent else colors.TextSecondary
                    )
                }
            }
        }
    }
}
