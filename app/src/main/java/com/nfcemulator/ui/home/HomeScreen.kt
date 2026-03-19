package com.nfcemulator.ui.home

import androidx.compose.animation.*
import androidx.compose.foundation.*
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.Edit
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.nfcemulator.ui.theme.NfcColors
import com.nfcemulator.ui.theme.NfcDimensions
import com.nfcemulator.ui.theme.NfcMonoStyles

@Composable
fun HomeScreen(
    tags: List<TagUiModel>,
    onTagClick: (String) -> Unit,
    onEmulateTag: (String) -> Unit,
    onEditTag: (String) -> Unit,
    onDeleteTag: (String) -> Unit,
    onRenameTag: (String, String) -> Unit,
    onAddClick: () -> Unit,
    onSearchQuery: (String) -> Unit
) {
    var searchQuery by remember { mutableStateOf("") }
    var contextMenuTagId by remember { mutableStateOf<String?>(null) }
    var renamingTagId by remember { mutableStateOf<String?>(null) }

    Box(modifier = Modifier.fillMaxSize()) {
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
                        TagCard(
                            tag = tag,
                            isRenaming = renamingTagId == tag.id,
                            onClick = { onTagClick(tag.id) },
                            onLongPress = { contextMenuTagId = tag.id },
                            onRenameSubmit = { newName ->
                                onRenameTag(tag.id, newName)
                                renamingTagId = null
                            },
                            onRenameDismiss = { renamingTagId = null }
                        )
                    }
                }
            }
        }

        // Context menu overlay
        if (contextMenuTagId != null) {
            val tag = tags.find { it.id == contextMenuTagId }
            if (tag != null) {
                ContextMenuOverlay(
                    tag = tag,
                    onDismiss = { contextMenuTagId = null },
                    onEmulate = {
                        contextMenuTagId = null
                        onEmulateTag(tag.id)
                    },
                    onEdit = {
                        contextMenuTagId = null
                        onEditTag(tag.id)
                    },
                    onRename = {
                        contextMenuTagId = null
                        renamingTagId = tag.id
                    },
                    onDelete = {
                        contextMenuTagId = null
                        onDeleteTag(tag.id)
                    }
                )
            }
        }
    }
}

@Composable
private fun ContextMenuOverlay(
    tag: TagUiModel,
    onDismiss: () -> Unit,
    onEmulate: () -> Unit,
    onEdit: () -> Unit,
    onRename: () -> Unit,
    onDelete: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(NfcColors.Background.copy(alpha = 0.85f))
            .clickable(onClick = onDismiss),
        contentAlignment = Alignment.Center
    ) {
        Column(
            modifier = Modifier
                .padding(horizontal = 32.dp)
                .fillMaxWidth()
                .clip(RoundedCornerShape(16.dp))
                .background(NfcColors.Surface)
                .border(1.dp, NfcColors.Primary.copy(alpha = 0.3f), RoundedCornerShape(16.dp))
                .clickable(enabled = false, onClick = {}),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Header
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(NfcColors.SurfaceVariant)
                    .padding(20.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    tag.name,
                    style = MaterialTheme.typography.titleMedium,
                    color = NfcColors.TextPrimary
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    tag.uid,
                    style = NfcMonoStyles.uid.copy(fontSize = MaterialTheme.typography.bodySmall.fontSize),
                    color = NfcColors.Secondary
                )
            }

            // Menu items
            ContextMenuItem(
                icon = Icons.Default.PlayArrow,
                label = "Emulate",
                color = NfcColors.Primary,
                onClick = onEmulate
            )
            ContextMenuDivider()
            ContextMenuItem(
                icon = Icons.Outlined.Edit,
                label = "Hex Editor",
                color = NfcColors.Primary,
                onClick = onEdit
            )
            ContextMenuDivider()
            ContextMenuItem(
                icon = Icons.Default.Edit,
                label = "Rename",
                color = NfcColors.TextPrimary,
                onClick = onRename
            )
            ContextMenuDivider()
            ContextMenuItem(
                icon = Icons.Default.Delete,
                label = "Delete",
                color = NfcColors.Error,
                onClick = onDelete
            )
        }
    }
}

@Composable
private fun ContextMenuItem(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    label: String,
    color: androidx.compose.ui.graphics.Color,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(horizontal = 20.dp, vertical = 16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = icon,
            contentDescription = label,
            tint = color,
            modifier = Modifier.size(22.dp)
        )
        Spacer(modifier = Modifier.width(16.dp))
        Text(
            label,
            style = MaterialTheme.typography.bodyLarge,
            color = color
        )
    }
}

@Composable
private fun ContextMenuDivider() {
    HorizontalDivider(
        color = NfcColors.Border,
        thickness = 0.5.dp,
        modifier = Modifier.padding(horizontal = 16.dp)
    )
}

@Composable
private fun TagCard(
    tag: TagUiModel,
    isRenaming: Boolean,
    onClick: () -> Unit,
    onLongPress: () -> Unit,
    onRenameSubmit: (String) -> Unit,
    onRenameDismiss: () -> Unit
) {
    var renameText by remember(isRenaming) {
        mutableStateOf(TextFieldValue(tag.name, TextRange(tag.name.length)))
    }
    val focusRequester = remember { FocusRequester() }

    LaunchedEffect(isRenaming) {
        if (isRenaming) {
            focusRequester.requestFocus()
        }
    }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(NfcDimensions.CornerRadius))
            .background(NfcColors.Surface)
            .border(
                NfcDimensions.BorderWidth,
                if (isRenaming) NfcColors.Primary else NfcColors.Border,
                RoundedCornerShape(NfcDimensions.CornerRadius)
            )
            .pointerInput(Unit) {
                detectTapGestures(
                    onTap = { if (!isRenaming) onClick() },
                    onLongPress = { if (!isRenaming) onLongPress() }
                )
            }
            .padding(NfcDimensions.CardPadding),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f)) {
            if (isRenaming) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    BasicTextField(
                        value = renameText,
                        onValueChange = { renameText = it },
                        textStyle = MaterialTheme.typography.titleMedium.copy(color = NfcColors.TextPrimary),
                        cursorBrush = SolidColor(NfcColors.Primary),
                        singleLine = true,
                        modifier = Modifier
                            .weight(1f)
                            .focusRequester(focusRequester)
                            .background(NfcColors.SurfaceVariant, RoundedCornerShape(8.dp))
                            .padding(horizontal = 8.dp, vertical = 6.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Icon(
                        Icons.Default.Check,
                        contentDescription = "Save",
                        tint = NfcColors.Secondary,
                        modifier = Modifier
                            .size(24.dp)
                            .clickable {
                                if (renameText.text.isNotBlank()) {
                                    onRenameSubmit(renameText.text.trim())
                                }
                            }
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Icon(
                        Icons.Default.Close,
                        contentDescription = "Cancel",
                        tint = NfcColors.TextSecondary,
                        modifier = Modifier
                            .size(24.dp)
                            .clickable { onRenameDismiss() }
                    )
                }
            } else {
                Text(
                    text = tag.name,
                    style = MaterialTheme.typography.titleMedium,
                    color = NfcColors.TextPrimary,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "UID: ${tag.uid}",
                style = NfcMonoStyles.uid.copy(fontSize = MaterialTheme.typography.bodySmall.fontSize),
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
        if (!isRenaming) {
            Icon(
                imageVector = Icons.Default.MoreVert,
                contentDescription = "Menu",
                tint = NfcColors.TextSecondary,
                modifier = Modifier
                    .size(24.dp)
                    .clickable { onLongPress() }
            )
        }
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
