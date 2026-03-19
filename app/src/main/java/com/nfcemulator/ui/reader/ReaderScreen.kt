package com.nfcemulator.ui.reader

import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.scale
import androidx.compose.ui.unit.dp
import com.nfcemulator.dump.model.TagDump
import com.nfcemulator.nfc.reader.ReadProgress
import com.nfcemulator.ui.theme.NfcColors
import com.nfcemulator.ui.theme.NfcDimensions
import com.nfcemulator.ui.theme.NfcMonoStyles

@Composable
fun ReaderScreen(
    readProgress: ReadProgress,
    onImportClick: () -> Unit,
    onSaveTag: (TagDump) -> Unit,
    onReset: () -> Unit,
    onCrackKeys: (TagDump) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(NfcColors.Background)
            .padding(NfcDimensions.Padding)
            .verticalScroll(rememberScrollState()),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "Read Tag",
            style = MaterialTheme.typography.headlineLarge,
            color = NfcColors.Primary
        )

        Spacer(modifier = Modifier.height(48.dp))

        NfcPulseAnimation(readProgress)

        Spacer(modifier = Modifier.height(32.dp))

        when (readProgress) {
            is ReadProgress.Idle -> {
                Text(
                    "Hold your device near an NFC tag",
                    style = MaterialTheme.typography.bodyLarge,
                    color = NfcColors.TextSecondary
                )
                Spacer(modifier = Modifier.height(48.dp))
                OutlinedButton(
                    onClick = onImportClick,
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = NfcColors.Primary),
                    border = ButtonDefaults.outlinedButtonBorder(enabled = true).copy(
                        brush = androidx.compose.ui.graphics.SolidColor(NfcColors.Primary)
                    ),
                    shape = RoundedCornerShape(NfcDimensions.CornerRadius),
                    modifier = Modifier.fillMaxWidth(0.7f)
                ) {
                    Text("Import Dump File")
                }
            }
            is ReadProgress.Reading -> {
                Text(
                    "Reading sector ${readProgress.sector}/${readProgress.total}",
                    style = MaterialTheme.typography.bodyLarge,
                    color = NfcColors.Primary
                )
                Spacer(modifier = Modifier.height(8.dp))
                LinearProgressIndicator(
                    progress = { readProgress.sector.toFloat() / readProgress.total },
                    modifier = Modifier.fillMaxWidth(0.6f),
                    color = NfcColors.Primary,
                    trackColor = NfcColors.SurfaceVariant
                )
            }
            is ReadProgress.KeyTesting -> {
                Text(
                    "Testing keys for sector ${readProgress.sector}",
                    style = MaterialTheme.typography.bodyLarge,
                    color = NfcColors.Warning
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    "${readProgress.keysTestedCount} keys tested",
                    style = MaterialTheme.typography.bodySmall,
                    color = NfcColors.TextSecondary
                )
            }
            is ReadProgress.Complete -> {
                CompleteContent(
                    dump = readProgress.dump,
                    onSaveTag = onSaveTag,
                    onReset = onReset,
                    onCrackKeys = onCrackKeys
                )
            }
            is ReadProgress.Error -> {
                Text(
                    "Error: ${readProgress.message}",
                    style = MaterialTheme.typography.bodyLarge,
                    color = NfcColors.Error
                )
                Spacer(modifier = Modifier.height(24.dp))
                OutlinedButton(
                    onClick = onReset,
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = NfcColors.Primary),
                    border = ButtonDefaults.outlinedButtonBorder(enabled = true).copy(
                        brush = androidx.compose.ui.graphics.SolidColor(NfcColors.Primary)
                    ),
                    shape = RoundedCornerShape(NfcDimensions.CornerRadius),
                    modifier = Modifier.fillMaxWidth(0.7f)
                ) {
                    Text("Try Again")
                }
            }
        }

        Spacer(modifier = Modifier.height(32.dp))
    }
}

@Composable
private fun CompleteContent(
    dump: TagDump,
    onSaveTag: (TagDump) -> Unit,
    onReset: () -> Unit,
    onCrackKeys: (TagDump) -> Unit
) {
    val allKeysFound = dump.totalKeys > 0 && dump.foundKeys == dump.totalKeys

    Text(
        "Tag read successfully!",
        style = MaterialTheme.typography.titleMedium,
        color = NfcColors.Secondary
    )

    Spacer(modifier = Modifier.height(16.dp))

    // Tag info card
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(NfcColors.Surface, RoundedCornerShape(NfcDimensions.CornerRadius))
            .border(NfcDimensions.BorderWidth, NfcColors.Secondary, RoundedCornerShape(NfcDimensions.CornerRadius))
            .padding(NfcDimensions.CardPadding),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            dump.uidHex,
            style = NfcMonoStyles.uid,
            color = NfcColors.Secondary
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            dump.type.displayName,
            style = MaterialTheme.typography.bodyMedium,
            color = NfcColors.TextSecondary
        )
        if (dump.sectors.isNotEmpty()) {
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                "${dump.sectors.size} sectors",
                style = MaterialTheme.typography.bodySmall,
                color = NfcColors.TextSecondary
            )
        }
        if (dump.totalKeys > 0) {
            Spacer(modifier = Modifier.height(12.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    "Keys",
                    style = MaterialTheme.typography.bodyMedium,
                    color = NfcColors.TextSecondary
                )
                Text(
                    "${dump.foundKeys}/${dump.totalKeys}",
                    style = MaterialTheme.typography.titleMedium,
                    color = if (allKeysFound) NfcColors.KeyFound else NfcColors.Warning
                )
            }
            Spacer(modifier = Modifier.height(6.dp))
            LinearProgressIndicator(
                progress = { dump.decryptionProgress },
                modifier = Modifier.fillMaxWidth().height(6.dp),
                color = if (allKeysFound) NfcColors.KeyFound else NfcColors.Warning,
                trackColor = NfcColors.SurfaceVariant
            )
        }
    }

    // Crack keys button (if keys missing)
    if (!allKeysFound && dump.totalKeys > 0) {
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            "${dump.totalKeys - dump.foundKeys} keys remaining — try extended dictionaries?",
            style = MaterialTheme.typography.bodySmall,
            color = NfcColors.TextSecondary
        )
        Spacer(modifier = Modifier.height(8.dp))
        OutlinedButton(
            onClick = { onCrackKeys(dump) },
            colors = ButtonDefaults.outlinedButtonColors(contentColor = NfcColors.Warning),
            border = ButtonDefaults.outlinedButtonBorder(enabled = true).copy(
                brush = androidx.compose.ui.graphics.SolidColor(NfcColors.Warning)
            ),
            shape = RoundedCornerShape(NfcDimensions.CornerRadius),
            modifier = Modifier.fillMaxWidth(0.7f)
        ) {
            Text("Crack Remaining Keys")
        }
    }

    // Action buttons
    Spacer(modifier = Modifier.height(24.dp))

    Button(
        onClick = { onSaveTag(dump) },
        colors = ButtonDefaults.buttonColors(
            containerColor = NfcColors.Primary,
            contentColor = NfcColors.Background
        ),
        shape = RoundedCornerShape(NfcDimensions.CornerRadius),
        modifier = Modifier.fillMaxWidth(0.7f).height(48.dp)
    ) {
        Text("Save Tag", style = MaterialTheme.typography.titleMedium)
    }

    Spacer(modifier = Modifier.height(12.dp))

    OutlinedButton(
        onClick = onReset,
        colors = ButtonDefaults.outlinedButtonColors(contentColor = NfcColors.Primary),
        border = ButtonDefaults.outlinedButtonBorder(enabled = true).copy(
            brush = androidx.compose.ui.graphics.SolidColor(NfcColors.Primary)
        ),
        shape = RoundedCornerShape(NfcDimensions.CornerRadius),
        modifier = Modifier.fillMaxWidth(0.7f).height(48.dp)
    ) {
        Text("Read Another", style = MaterialTheme.typography.titleMedium)
    }
}

@Composable
private fun NfcPulseAnimation(readProgress: ReadProgress) {
    val infiniteTransition = rememberInfiniteTransition(label = "pulse")
    val isActive = readProgress is ReadProgress.Idle || readProgress is ReadProgress.Reading

    val scale by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = if (isActive) 2.5f else 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(1500, easing = EaseOut),
            repeatMode = RepeatMode.Restart
        ),
        label = "scale"
    )

    val alpha by infiniteTransition.animateFloat(
        initialValue = 0.6f,
        targetValue = 0f,
        animationSpec = infiniteRepeatable(
            animation = tween(1500, easing = EaseOut),
            repeatMode = RepeatMode.Restart
        ),
        label = "alpha"
    )

    val color = when (readProgress) {
        is ReadProgress.Complete -> NfcColors.Secondary
        is ReadProgress.Error -> NfcColors.Error
        else -> NfcColors.Primary
    }

    val labelText = when (readProgress) {
        is ReadProgress.Complete -> "OK"
        else -> "NFC"
    }

    Box(contentAlignment = Alignment.Center, modifier = Modifier.height(120.dp)) {
        if (isActive) {
            Box(
                modifier = Modifier
                    .size(120.dp)
                    .scale(scale)
                    .alpha(alpha)
                    .border(2.dp, color, CircleShape)
            )
        }
        Box(
            modifier = Modifier
                .size(80.dp)
                .border(3.dp, color, CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Text(labelText, style = MaterialTheme.typography.titleLarge, color = color)
        }
    }
}
