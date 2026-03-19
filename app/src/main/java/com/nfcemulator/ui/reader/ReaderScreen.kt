package com.nfcemulator.ui.reader

import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
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
            .padding(NfcDimensions.Padding),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "Read Tag",
            style = MaterialTheme.typography.headlineLarge,
            color = NfcColors.Primary
        )

        Spacer(modifier = Modifier.weight(1f))

        NfcPulseAnimation(readProgress)

        Spacer(modifier = Modifier.height(32.dp))

        when (readProgress) {
            is ReadProgress.Idle -> {
                Text(
                    "Hold your device near an NFC tag",
                    style = MaterialTheme.typography.bodyLarge,
                    color = NfcColors.TextSecondary
                )
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
                Text(
                    "${readProgress.keysTestedCount} keys tested",
                    style = MaterialTheme.typography.bodySmall,
                    color = NfcColors.TextSecondary
                )
            }
            is ReadProgress.Complete -> {
                val dump = readProgress.dump
                val allKeysFound = dump.foundKeys == dump.totalKeys

                Text(
                    "Tag read successfully!",
                    style = MaterialTheme.typography.bodyLarge,
                    color = NfcColors.Secondary
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    dump.uidHex,
                    style = NfcMonoStyles.uid,
                    color = NfcColors.Secondary
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    dump.type.displayName,
                    style = MaterialTheme.typography.bodyMedium,
                    color = NfcColors.TextSecondary
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    "Keys: ${dump.foundKeys}/${dump.totalKeys} found",
                    style = MaterialTheme.typography.bodyMedium,
                    color = NfcColors.TextPrimary
                )
                if (dump.totalKeys > 0) {
                    Spacer(modifier = Modifier.height(4.dp))
                    LinearProgressIndicator(
                        progress = { dump.decryptionProgress },
                        modifier = Modifier.fillMaxWidth(0.6f),
                        color = if (allKeysFound) NfcColors.KeyFound else NfcColors.Warning,
                        trackColor = NfcColors.SurfaceVariant
                    )
                }
                if (!allKeysFound && dump.totalKeys > 0) {
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        "Continue cracking remaining keys?",
                        style = MaterialTheme.typography.bodySmall,
                        color = NfcColors.TextSecondary
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedButton(
                        onClick = { onCrackKeys(dump) },
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = NfcColors.Warning),
                        border = ButtonDefaults.outlinedButtonBorder.copy(
                            brush = androidx.compose.ui.graphics.SolidColor(NfcColors.Warning)
                        ),
                        shape = RoundedCornerShape(NfcDimensions.CornerRadius)
                    ) {
                        Text("Crack Keys")
                    }
                }
                Spacer(modifier = Modifier.height(16.dp))
                Button(
                    onClick = { onSaveTag(dump) },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = NfcColors.Primary,
                        contentColor = NfcColors.Background
                    ),
                    shape = RoundedCornerShape(NfcDimensions.CornerRadius),
                    modifier = Modifier.fillMaxWidth(0.6f)
                ) {
                    Text("Save Tag")
                }
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedButton(
                    onClick = onReset,
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = NfcColors.Primary),
                    border = ButtonDefaults.outlinedButtonBorder.copy(
                        brush = androidx.compose.ui.graphics.SolidColor(NfcColors.Primary)
                    ),
                    shape = RoundedCornerShape(NfcDimensions.CornerRadius),
                    modifier = Modifier.fillMaxWidth(0.6f)
                ) {
                    Text("Read Another")
                }
            }
            is ReadProgress.Error -> {
                Text(
                    "Error: ${readProgress.message}",
                    style = MaterialTheme.typography.bodyLarge,
                    color = NfcColors.Error
                )
                Spacer(modifier = Modifier.height(16.dp))
                OutlinedButton(
                    onClick = onReset,
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = NfcColors.Primary),
                    border = ButtonDefaults.outlinedButtonBorder.copy(
                        brush = androidx.compose.ui.graphics.SolidColor(NfcColors.Primary)
                    ),
                    shape = RoundedCornerShape(NfcDimensions.CornerRadius)
                ) {
                    Text("Try Again")
                }
            }
        }

        Spacer(modifier = Modifier.weight(1f))

        if (readProgress is ReadProgress.Idle) {
            OutlinedButton(
                onClick = onImportClick,
                colors = ButtonDefaults.outlinedButtonColors(contentColor = NfcColors.Primary),
                border = ButtonDefaults.outlinedButtonBorder.copy(
                    brush = androidx.compose.ui.graphics.SolidColor(NfcColors.Primary)
                ),
                shape = RoundedCornerShape(NfcDimensions.CornerRadius)
            ) {
                Text("Import Dump File")
            }

            Spacer(modifier = Modifier.height(16.dp))
        }
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

    Box(contentAlignment = Alignment.Center) {
        if (isActive) {
            Box(
                modifier = Modifier
                    .size(NfcDimensions.PulseMaxSize)
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
