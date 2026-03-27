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
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.nfcemulator.R
import com.nfcemulator.ui.theme.LocalAppColors
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
    val colors = LocalAppColors.current
    var searchQuery by remember { mutableStateOf("") }
    var contextMenuTagId by remember { mutableStateOf<String?>(null) }
    var renamingTagId by remember { mutableStateOf<String?>(null) }

    Box(modifier = Modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(colors.Background)
                .padding(NfcDimensions.Padding)
        ) {
            Text(
                text = stringResource(R.string.app_name),
                style = MaterialTheme.typography.headlineLarge,
                color = colors.Primary
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Search field — pill shape with SurfaceContainer fill + clear button
            OutlinedTextField(
                value = searchQuery,
                onValueChange = {
                    searchQuery = it
                    onSearchQuery(it)
                },
                placeholder = {
                    Text(
                        stringResource(R.string.search_tags),
                        color = colors.TextSecondary
                    )
                },
                leadingIcon = {
                    Icon(
                        Icons.Default.Search,
                        contentDescription = "Search",
                        tint = colors.Primary
                    )
                },
                trailingIcon = {
                    AnimatedVisibility(
                        visible = searchQuery.isNotEmpty(),
                        enter = fadeIn() + scaleIn(),
                        exit = fadeOut() + scaleOut()
                    ) {
                        IconButton(onClick = {
                            searchQuery = ""
                            onSearchQuery("")
                        }) {
                            Icon(
                                Icons.Default.Clear,
                                contentDescription = "Clear search",
                                tint = colors.TextSecondary
                            )
                        }
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .background(colors.SurfaceContainer, RoundedCornerShape(28.dp)),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = colors.Primary,
                    unfocusedBorderColor = colors.Border,
                    cursorColor = colors.Primary,
                    focusedTextColor = colors.TextPrimary,
                    unfocusedTextColor = colors.TextPrimary,
                    focusedContainerColor = colors.SurfaceContainer,
                    unfocusedContainerColor = colors.SurfaceContainer
                ),
                shape = RoundedCornerShape(28.dp),
                singleLine = true
            )

            Spacer(modifier = Modifier.height(16.dp))

            if (tags.isEmpty()) {
                // Empty state
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Search,
                            contentDescription = null,
                            tint = colors.Primary.copy(alpha = 0.25f),
                            modifier = Modifier.size(NfcDimensions.IconSizeXLarge)
                        )
                        Text(
                            stringResource(R.string.no_tags_saved),
                            style = MaterialTheme.typography.titleMedium,
                            color = colors.TextSecondary,
                            textAlign = TextAlign.Center
                        )
                        Text(
                            stringResource(R.string.no_tags_hint),
                            style = MaterialTheme.typography.bodyMedium,
                            color = colors.TextSecondary.copy(alpha = 0.6f),
                            textAlign = TextAlign.Center
                        )
                    }
                }
            } else {
                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                    contentPadding = PaddingValues(bottom = 88.dp)
                ) {
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

        // FAB — bottom-right
        FloatingActionButton(
            onClick = onAddClick,
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(NfcDimensions.PaddingLarge),
            containerColor = colors.Primary,
            contentColor = colors.Background,
            shape = RoundedCornerShape(NfcDimensions.CornerRadiusExtraLarge)
        ) {
            Icon(
                imageVector = Icons.Default.Add,
                contentDescription = stringResource(R.string.app_name),
                modifier = Modifier.size(NfcDimensions.IconSize)
            )
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
    val colors = LocalAppColors.current

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(colors.Background.copy(alpha = 0.88f))
            .clickable(onClick = onDismiss),
        contentAlignment = Alignment.Center
    ) {
        Column(
            modifier = Modifier
                .padding(horizontal = 32.dp)
                .fillMaxWidth()
                .clip(RoundedCornerShape(NfcDimensions.CornerRadiusExtraLarge))
                .background(colors.SurfaceContainerHigh)
                .border(
                    NfcDimensions.BorderWidth,
                    colors.Primary.copy(alpha = 0.35f),
                    RoundedCornerShape(NfcDimensions.CornerRadiusExtraLarge)
                )
                .clickable(enabled = false, onClick = {}),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Header
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(
                        colors.SurfaceVariant,
                        RoundedCornerShape(
                            topStart = NfcDimensions.CornerRadiusExtraLarge,
                            topEnd = NfcDimensions.CornerRadiusExtraLarge
                        )
                    )
                    .padding(20.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    tag.name,
                    style = MaterialTheme.typography.titleMedium,
                    color = colors.TextPrimary
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    tag.uid,
                    style = NfcMonoStyles.uid.copy(fontSize = MaterialTheme.typography.bodySmall.fontSize),
                    color = colors.Secondary
                )
            }

            // Menu items
            ContextMenuItem(
                icon = Icons.Default.PlayArrow,
                label = stringResource(R.string.emulate),
                color = colors.Primary,
                onClick = onEmulate
            )
            ContextMenuDivider()
            ContextMenuItem(
                icon = Icons.Outlined.Edit,
                label = stringResource(R.string.hex_editor_menu),
                color = colors.Primary,
                onClick = onEdit
            )
            ContextMenuDivider()
            ContextMenuItem(
                icon = Icons.Default.Edit,
                label = stringResource(R.string.rename),
                color = colors.TextPrimary,
                onClick = onRename
            )
            ContextMenuDivider()
            ContextMenuItem(
                icon = Icons.Default.Delete,
                label = stringResource(R.string.delete),
                color = colors.Error,
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
        color = LocalAppColors.current.Border,
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
    val colors = LocalAppColors.current
    var renameText by remember(isRenaming) {
        mutableStateOf(TextFieldValue(tag.name, TextRange(tag.name.length)))
    }
    val focusRequester = remember { FocusRequester() }

    LaunchedEffect(isRenaming) {
        if (isRenaming) {
            focusRequester.requestFocus()
        }
    }

    OutlinedCard(
        modifier = Modifier
            .fillMaxWidth()
            .pointerInput(Unit) {
                detectTapGestures(
                    onTap = { if (!isRenaming) onClick() },
                    onLongPress = { if (!isRenaming) onLongPress() }
                )
            },
        shape = RoundedCornerShape(NfcDimensions.CornerRadius),
        colors = CardDefaults.outlinedCardColors(
            containerColor = colors.SurfaceContainer
        ),
        border = BorderStroke(
            width = if (isRenaming) NfcDimensions.BorderWidthActive else NfcDimensions.BorderWidth,
            color = if (isRenaming) colors.BorderActive else colors.Border
        )
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Left accent bar
            Box(
                modifier = Modifier
                    .width(4.dp)
                    .height(72.dp)
                    .background(
                        color = colors.Accent,
                        shape = RoundedCornerShape(
                            topStart = NfcDimensions.CornerRadius,
                            bottomStart = NfcDimensions.CornerRadius
                        )
                    )
            )

            Column(
                modifier = Modifier
                    .weight(1f)
                    .padding(NfcDimensions.CardPadding)
            ) {
                if (isRenaming) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        BasicTextField(
                            value = renameText,
                            onValueChange = { renameText = it },
                            textStyle = MaterialTheme.typography.titleMedium.copy(color = colors.TextPrimary),
                            cursorBrush = SolidColor(colors.Primary),
                            singleLine = true,
                            modifier = Modifier
                                .weight(1f)
                                .focusRequester(focusRequester)
                                .background(colors.SurfaceVariant, RoundedCornerShape(8.dp))
                                .padding(horizontal = 8.dp, vertical = 6.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Icon(
                            Icons.Default.Check,
                            contentDescription = "Save",
                            tint = colors.Secondary,
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
                            tint = colors.TextSecondary,
                            modifier = Modifier
                                .size(24.dp)
                                .clickable { onRenameDismiss() }
                        )
                    }
                } else {
                    Text(
                        text = tag.name,
                        style = MaterialTheme.typography.titleMedium,
                        color = colors.TextPrimary,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "UID: ${tag.uid}",
                    style = NfcMonoStyles.uid.copy(fontSize = MaterialTheme.typography.bodySmall.fontSize),
                    color = colors.Secondary
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = tag.type,
                    style = MaterialTheme.typography.bodySmall,
                    color = colors.TextSecondary
                )
                if (tag.keysTotal > 0) {
                    Spacer(modifier = Modifier.height(8.dp))
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        LinearProgressIndicator(
                            progress = { tag.keysFound.toFloat() / tag.keysTotal },
                            modifier = Modifier
                                .weight(1f)
                                .height(4.dp)
                                .clip(RoundedCornerShape(2.dp)),
                            color = colors.Secondary,
                            trackColor = colors.SurfaceVariant
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "${tag.keysFound}/${tag.keysTotal}",
                            style = MaterialTheme.typography.labelSmall,
                            color = colors.TextSecondary
                        )
                    }
                }
            }

            if (!isRenaming) {
                IconButton(
                    onClick = onLongPress,
                    modifier = Modifier.padding(end = 4.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.MoreVert,
                        contentDescription = "Menu",
                        tint = colors.TextSecondary,
                        modifier = Modifier.size(NfcDimensions.IconSize)
                    )
                }
            }
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
