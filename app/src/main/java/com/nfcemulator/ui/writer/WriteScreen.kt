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
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
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
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(LocalAppColors.current.Background)
            .padding(NfcDimensions.Padding),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text("Write to Card", style = MaterialTheme.typography.headlineLarge, color = LocalAppColors.current.Accent)

        Spacer(modifier = Modifier.height(4.dp))

        Text(
            "Clone a saved tag to a blank magic card",
            style = MaterialTheme.typography.bodySmall,
            color = LocalAppColors.current.TextSecondary
        )

        Spacer(modifier = Modifier.height(16.dp))

        if (selectedTag != null) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(LocalAppColors.current.Surface, RoundedCornerShape(NfcDimensions.CornerRadius))
                    .border(NfcDimensions.BorderWidth, LocalAppColors.current.Accent, RoundedCornerShape(NfcDimensions.CornerRadius))
                    .padding(NfcDimensions.CardPadding),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text("Selected:", style = MaterialTheme.typography.labelSmall, color = LocalAppColors.current.TextSecondary)
                Spacer(modifier = Modifier.height(4.dp))
                Text(selectedTag.name, style = MaterialTheme.typography.titleMedium, color = LocalAppColors.current.TextPrimary)
                Text("UID: ${selectedTag.uid}", style = NfcMonoStyles.uid.copy(fontSize = MaterialTheme.typography.bodySmall.fontSize), color = LocalAppColors.current.Secondary)
                Text(selectedTag.type, style = MaterialTheme.typography.bodySmall, color = LocalAppColors.current.TextSecondary)
            }

            Spacer(modifier = Modifier.height(24.dp))

            val isWaiting = writeProgress.contains("Waiting") || writeProgress.contains("Writing")

            Button(
                onClick = if (isWaiting) onCancelWrite else onStartWrite,
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (isWaiting) LocalAppColors.current.Error else LocalAppColors.current.Accent
                ),
                shape = RoundedCornerShape(NfcDimensions.CornerRadius),
                modifier = Modifier
                    .fillMaxWidth(0.7f)
                    .height(52.dp)
            ) {
                Text(
                    if (isWaiting) "Cancel" else "Write to Magic Tag",
                    style = MaterialTheme.typography.titleMedium,
                    color = LocalAppColors.current.Background
                )
            }

            if (writeProgress.isNotEmpty()) {
                Spacer(modifier = Modifier.height(12.dp))
                val color = when {
                    writeProgress.contains("Error") || writeProgress.contains("Cannot") || writeProgress.contains("Failed") -> LocalAppColors.current.Error
                    writeProgress.contains("complete") || writeProgress.contains("Complete") -> LocalAppColors.current.Secondary
                    writeProgress.contains("Waiting") -> LocalAppColors.current.Warning
                    else -> LocalAppColors.current.Accent
                }
                Text(writeProgress, style = MaterialTheme.typography.bodyMedium, color = color)
            }

            Spacer(modifier = Modifier.height(24.dp))

            Text(
                "Select another tag:",
                style = MaterialTheme.typography.labelMedium,
                color = LocalAppColors.current.TextSecondary,
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(8.dp))
        } else {
            Text(
                "Select a tag to write:",
                style = MaterialTheme.typography.titleMedium,
                color = LocalAppColors.current.TextSecondary
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
                        .background(if (isSelected) LocalAppColors.current.SurfaceVariant else LocalAppColors.current.Surface)
                        .border(
                            NfcDimensions.BorderWidth,
                            if (isSelected) LocalAppColors.current.Accent else LocalAppColors.current.Border,
                            RoundedCornerShape(NfcDimensions.CornerRadiusSmall)
                        )
                        .clickable { onSelectTag(tag) }
                        .padding(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(tag.name, style = MaterialTheme.typography.bodyMedium, color = LocalAppColors.current.TextPrimary, maxLines = 1, overflow = TextOverflow.Ellipsis)
                        Text("UID: ${tag.uid}", style = MaterialTheme.typography.bodySmall, color = LocalAppColors.current.TextSecondary)
                    }
                    Text(tag.type, style = MaterialTheme.typography.labelSmall, color = LocalAppColors.current.TextSecondary)
                }
            }
        }
    }
}
