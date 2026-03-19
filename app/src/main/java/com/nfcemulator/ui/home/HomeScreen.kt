package com.nfcemulator.ui.home

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.nfcemulator.ui.theme.NfcColors
import com.nfcemulator.ui.theme.NfcDimensions
import com.nfcemulator.ui.theme.NfcMonoStyles

@Composable
fun HomeScreen(
    tags: List<TagUiModel>,
    onTagClick: (String) -> Unit,
    onAddClick: () -> Unit,
    onSearchQuery: (String) -> Unit
) {
    var searchQuery by remember { mutableStateOf("") }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(NfcColors.Background)
            .padding(NfcDimensions.Padding)
    ) {
        Text(
            text = "NFC Emulator",
            style = MaterialTheme.typography.headlineLarge,
            color = NfcColors.Primary
        )

        Spacer(modifier = Modifier.height(16.dp))

        OutlinedTextField(
            value = searchQuery,
            onValueChange = {
                searchQuery = it
                onSearchQuery(it)
            },
            placeholder = { Text("Search tags...", color = NfcColors.TextSecondary) },
            leadingIcon = { Icon(Icons.Default.Search, contentDescription = "Search", tint = NfcColors.Primary) },
            modifier = Modifier.fillMaxWidth(),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = NfcColors.Primary,
                unfocusedBorderColor = NfcColors.Border,
                cursorColor = NfcColors.Primary,
                focusedTextColor = NfcColors.TextPrimary,
                unfocusedTextColor = NfcColors.TextPrimary
            ),
            shape = RoundedCornerShape(NfcDimensions.CornerRadius),
            singleLine = true
        )

        Spacer(modifier = Modifier.height(16.dp))

        if (tags.isEmpty()) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("No tags saved", style = MaterialTheme.typography.titleMedium, color = NfcColors.TextSecondary)
                    Spacer(modifier = Modifier.height(8.dp))
                    Text("Read a tag or import a dump file", style = MaterialTheme.typography.bodyMedium, color = NfcColors.TextSecondary)
                }
            }
        } else {
            LazyColumn(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                items(tags, key = { it.id }) { tag ->
                    TagCard(tag = tag, onClick = { onTagClick(tag.id) })
                }
            }
        }
    }
}

@Composable
private fun TagCard(tag: TagUiModel, onClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(NfcDimensions.CornerRadius))
            .background(NfcColors.Surface)
            .border(NfcDimensions.BorderWidth, NfcColors.Border, RoundedCornerShape(NfcDimensions.CornerRadius))
            .clickable(onClick = onClick)
            .padding(NfcDimensions.CardPadding),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = tag.name,
                style = MaterialTheme.typography.titleMedium,
                color = NfcColors.TextPrimary,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "UID: ${tag.uid}",
                style = NfcMonoStyles.uid.copy(fontSize = androidx.compose.ui.unit.TextUnit.Unspecified),
                color = NfcColors.Secondary
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = tag.type,
                style = MaterialTheme.typography.bodySmall,
                color = NfcColors.TextSecondary
            )
            if (tag.keysTotal > 0) {
                Spacer(modifier = Modifier.height(6.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    LinearProgressIndicator(
                        progress = { tag.keysFound.toFloat() / tag.keysTotal },
                        modifier = Modifier.weight(1f).height(4.dp),
                        color = NfcColors.Secondary,
                        trackColor = NfcColors.SurfaceVariant
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "${tag.keysFound}/${tag.keysTotal}",
                        style = MaterialTheme.typography.labelSmall,
                        color = NfcColors.TextSecondary
                    )
                }
            }
        }
        Icon(
            imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
            contentDescription = "Open",
            tint = NfcColors.TextSecondary,
            modifier = Modifier.size(24.dp)
        )
    }
}

data class TagUiModel(
    val id: String,
    val name: String,
    val uid: String,
    val type: String,
    val category: String,
    val keysFound: Int,
    val keysTotal: Int,
    val lastEmulated: String?
)
