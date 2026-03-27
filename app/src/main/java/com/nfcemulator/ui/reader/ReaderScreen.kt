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
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.nfcemulator.R
import com.nfcemulator.dump.model.TagDump
import com.nfcemulator.nfc.reader.ReadProgress
import com.nfcemulator.ui.theme.LocalAppColors
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
            .background(LocalAppColors.current.Background)
            .padding(NfcDimensions.Padding)
            .verticalScroll(rememberScrollState()),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = stringResource(R.string.read_tag),
            style = MaterialTheme.typography.headlineLarge,
            color = LocalAppColors.current.Primary
        )

        Spacer(modifier = Modifier.height(48.dp))

        NfcPulseAnimation(readProgress)

        Spacer(modifier = Modifier.height(32.dp))

        when (readProgress) {
            is ReadProgress.Idle -> {
                Text(
                    stringResource(R.string.hold_near_tag),
                    style = MaterialTheme.typography.bodyLarge,
                    color = LocalAppColors.current.TextSecondary
                )
                Spacer(modifier = Modifier.height(48.dp))
                Button(
                    onClick = onImportClick,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = LocalAppColors.current.Primary,
                        contentColor = LocalAppColors.current.Background
                    ),
                    shape = RoundedCornerShape(28.dp),
                    modifier = Modifier.fillMaxWidth(0.7f).height(48.dp)
                ) {
                    Text(stringResource(R.string.import_dump_file), fontWeight = FontWeight.SemiBold)
                }
            }
            is ReadProgress.Reading -> {
                Text(
                    "Reading sector ${readProgress.sector}/${readProgress.total}",
                    style = MaterialTheme.typography.bodyLarge,
                    color = LocalAppColors.current.Primary
                )
                Spacer(modifier = Modifier.height(8.dp))
                LinearProgressIndicator(
                    progress = { readProgress.sector.toFloat() / readProgress.total },
                    modifier = Modifier.fillMaxWidth(0.6f).height(6.dp),
                    color = LocalAppColors.current.Primary,
                    trackColor = LocalAppColors.current.SurfaceVariant
                )
            }
            is ReadProgress.KeyTesting -> {
                Text(
                    "Testing keys for sector ${readProgress.sector}",
                    style = MaterialTheme.typography.bodyLarge,
                    color = LocalAppColors.current.Warning
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    "${readProgress.keysTestedCount} keys tested",
                    style = MaterialTheme.typography.bodySmall,
                    color = LocalAppColors.current.TextSecondary
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
                    color = LocalAppColors.current.Error
                )
                Spacer(modifier = Modifier.height(24.dp))
                FilledTonalButton(
                    onClick = onReset,
                    colors = ButtonDefaults.filledTonalButtonColors(
                        containerColor = LocalAppColors.current.Primary.copy(alpha = 0.15f),
                        contentColor = LocalAppColors.current.Primary
                    ),
                    shape = RoundedCornerShape(28.dp),
                    modifier = Modifier.fillMaxWidth(0.7f).height(48.dp)
                ) {
                    Text(stringResource(R.string.try_again), fontWeight = FontWeight.SemiBold)
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
    val colors = LocalAppColors.current
    val allKeysFound = dump.totalKeys > 0 && dump.foundKeys == dump.totalKeys

    Text(
        stringResource(R.string.tag_read_success),
        style = MaterialTheme.typography.titleMedium,
        color = colors.Secondary
    )

    Spacer(modifier = Modifier.height(16.dp))

    // Tag info card — ElevatedCard-style with colored top accent
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(colors.SurfaceContainer, RoundedCornerShape(NfcDimensions.CornerRadius))
            .border(NfcDimensions.BorderWidth, colors.Secondary, RoundedCornerShape(NfcDimensions.CornerRadius)),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Colored top accent bar
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(4.dp)
                .background(
                    colors.Secondary,
                    RoundedCornerShape(topStart = NfcDimensions.CornerRadius, topEnd = NfcDimensions.CornerRadius)
                )
        )
        Column(
            modifier = Modifier.padding(NfcDimensions.CardPadding),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                dump.uidHex,
                style = NfcMonoStyles.uid,
                color = colors.Secondary
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                dump.type.displayName,
                style = MaterialTheme.typography.bodyMedium,
                color = colors.TextSecondary
            )
            if (dump.sectors.isNotEmpty()) {
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    "${dump.sectors.size} sectors",
                    style = MaterialTheme.typography.bodySmall,
                    color = colors.TextSecondary
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
                        stringResource(R.string.total_keys),
                        style = MaterialTheme.typography.bodyMedium,
                        color = colors.TextSecondary
                    )
                    Text(
                        "${dump.foundKeys}/${dump.totalKeys}",
                        style = MaterialTheme.typography.titleMedium,
                        color = if (allKeysFound) colors.KeyFound else colors.Warning
                    )
                }
                Spacer(modifier = Modifier.height(6.dp))
                LinearProgressIndicator(
                    progress = { dump.decryptionProgress },
                    modifier = Modifier.fillMaxWidth().height(6.dp),
                    color = if (allKeysFound) colors.KeyFound else colors.Warning,
                    trackColor = colors.SurfaceVariant
                )
            }
        }
    }

    // Crack keys button (if keys missing)
    if (!allKeysFound && dump.totalKeys > 0) {
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            "${dump.totalKeys - dump.foundKeys} keys remaining — try extended dictionaries?",
            style = MaterialTheme.typography.bodySmall,
            color = colors.TextSecondary
        )
        Spacer(modifier = Modifier.height(8.dp))
        FilledTonalButton(
            onClick = { onCrackKeys(dump) },
            colors = ButtonDefaults.filledTonalButtonColors(
                containerColor = colors.Warning.copy(alpha = 0.15f),
                contentColor = colors.Warning
            ),
            shape = RoundedCornerShape(28.dp),
            modifier = Modifier.fillMaxWidth(0.7f)
        ) {
            Text(stringResource(R.string.crack_keys), fontWeight = FontWeight.SemiBold)
        }
    }

    // Action buttons
    Spacer(modifier = Modifier.height(24.dp))

    Button(
        onClick = { onSaveTag(dump) },
        colors = ButtonDefaults.buttonColors(
            containerColor = colors.Primary,
            contentColor = colors.Background
        ),
        shape = RoundedCornerShape(28.dp),
        modifier = Modifier.fillMaxWidth(0.7f).height(48.dp)
    ) {
        Text(stringResource(R.string.save_tag), style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
    }

    Spacer(modifier = Modifier.height(12.dp))

    FilledTonalButton(
        onClick = onReset,
        colors = ButtonDefaults.filledTonalButtonColors(
            containerColor = colors.Primary.copy(alpha = 0.12f),
            contentColor = colors.Primary
        ),
        shape = RoundedCornerShape(28.dp),
        modifier = Modifier.fillMaxWidth(0.7f).height(48.dp)
    ) {
        Text(stringResource(R.string.read_another), style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
    }
}

@Composable
private fun NfcPulseAnimation(readProgress: ReadProgress) {
    val infiniteTransition = rememberInfiniteTransition(label = "pulse")
    val isActive = readProgress is ReadProgress.Idle || readProgress is ReadProgress.Reading

    val scale1 by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = if (isActive) 2.5f else 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(1500, easing = EaseOut),
            repeatMode = RepeatMode.Restart
        ),
        label = "scale1"
    )

    val scale2 by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = if (isActive) 2.0f else 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(1500, delayMillis = 300, easing = EaseOut),
            repeatMode = RepeatMode.Restart
        ),
        label = "scale2"
    )

    val scale3 by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = if (isActive) 1.6f else 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(1500, delayMillis = 600, easing = EaseOut),
            repeatMode = RepeatMode.Restart
        ),
        label = "scale3"
    )

    val alpha1 by infiniteTransition.animateFloat(
        initialValue = 0.6f,
        targetValue = 0f,
        animationSpec = infiniteRepeatable(
            animation = tween(1500, easing = EaseOut),
            repeatMode = RepeatMode.Restart
        ),
        label = "alpha1"
    )

    val alpha2 by infiniteTransition.animateFloat(
        initialValue = 0.5f,
        targetValue = 0f,
        animationSpec = infiniteRepeatable(
            animation = tween(1500, delayMillis = 300, easing = EaseOut),
            repeatMode = RepeatMode.Restart
        ),
        label = "alpha2"
    )

    val alpha3 by infiniteTransition.animateFloat(
        initialValue = 0.4f,
        targetValue = 0f,
        animationSpec = infiniteRepeatable(
            animation = tween(1500, delayMillis = 600, easing = EaseOut),
            repeatMode = RepeatMode.Restart
        ),
        label = "alpha3"
    )

    val colors = LocalAppColors.current

    val ringColor1 = when (readProgress) {
        is ReadProgress.Complete -> colors.Secondary
        is ReadProgress.Error -> colors.Error
        else -> colors.Primary
    }
    val ringColor2 = when (readProgress) {
        is ReadProgress.Complete -> colors.Secondary.copy(alpha = 0.7f)
        is ReadProgress.Error -> colors.Error.copy(alpha = 0.7f)
        else -> colors.Accent.copy(alpha = 0.7f)
    }
    val ringColor3 = when (readProgress) {
        is ReadProgress.Complete -> colors.Secondary.copy(alpha = 0.5f)
        is ReadProgress.Error -> colors.Error.copy(alpha = 0.5f)
        else -> colors.Accent.copy(alpha = 0.5f)
    }

    val centerColor = when (readProgress) {
        is ReadProgress.Complete -> colors.Secondary
        is ReadProgress.Error -> colors.Error
        else -> colors.Primary
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
                    .scale(scale1)
                    .alpha(alpha1)
                    .border(2.dp, ringColor1, CircleShape)
            )
            Box(
                modifier = Modifier
                    .size(120.dp)
                    .scale(scale2)
                    .alpha(alpha2)
                    .border(1.5.dp, ringColor2, CircleShape)
            )
            Box(
                modifier = Modifier
                    .size(120.dp)
                    .scale(scale3)
                    .alpha(alpha3)
                    .border(1.dp, ringColor3, CircleShape)
            )
        }
        Box(
            modifier = Modifier
                .size(80.dp)
                .background(colors.SurfaceContainer, CircleShape)
                .border(3.dp, centerColor, CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Text(labelText, style = MaterialTheme.typography.titleLarge, color = centerColor, fontWeight = FontWeight.Bold)
        }
    }
}
