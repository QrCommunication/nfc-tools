package com.nfcemulator.ui.splash

import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.nfcemulator.R
import com.nfcemulator.ui.theme.LocalAppColors
import kotlinx.coroutines.delay

@Composable
fun SplashScreen(onFinished: () -> Unit) {
    val appColors = LocalAppColors.current

    // Entry animation
    var startAnimation by remember { mutableStateOf(false) }
    val contentAlpha by animateFloatAsState(
        targetValue = if (startAnimation) 1f else 0f,
        animationSpec = tween(durationMillis = 600),
        label = "contentAlpha"
    )
    val contentScale by animateFloatAsState(
        targetValue = if (startAnimation) 1f else 0.7f,
        animationSpec = tween(durationMillis = 700, easing = EaseOutBack),
        label = "contentScale"
    )

    // Pulse ring 1 — fastest
    val ring1 = rememberInfiniteTransition(label = "ring1")
    val ring1Scale by ring1.animateFloat(
        initialValue = 0.6f,
        targetValue = 1.8f,
        animationSpec = infiniteRepeatable(
            animation = tween(1400, easing = EaseOut),
            repeatMode = RepeatMode.Restart
        ),
        label = "ring1Scale"
    )
    val ring1Alpha by ring1.animateFloat(
        initialValue = 0.7f,
        targetValue = 0f,
        animationSpec = infiniteRepeatable(
            animation = tween(1400, easing = EaseOut),
            repeatMode = RepeatMode.Restart
        ),
        label = "ring1Alpha"
    )

    // Pulse ring 2 — medium delay
    val ring2 = rememberInfiniteTransition(label = "ring2")
    val ring2Scale by ring2.animateFloat(
        initialValue = 0.6f,
        targetValue = 1.8f,
        animationSpec = infiniteRepeatable(
            animation = tween(1400, easing = EaseOut, delayMillis = 467),
            repeatMode = RepeatMode.Restart
        ),
        label = "ring2Scale"
    )
    val ring2Alpha by ring2.animateFloat(
        initialValue = 0.7f,
        targetValue = 0f,
        animationSpec = infiniteRepeatable(
            animation = tween(1400, easing = EaseOut, delayMillis = 467),
            repeatMode = RepeatMode.Restart
        ),
        label = "ring2Alpha"
    )

    // Pulse ring 3 — latest delay
    val ring3 = rememberInfiniteTransition(label = "ring3")
    val ring3Scale by ring3.animateFloat(
        initialValue = 0.6f,
        targetValue = 1.8f,
        animationSpec = infiniteRepeatable(
            animation = tween(1400, easing = EaseOut, delayMillis = 934),
            repeatMode = RepeatMode.Restart
        ),
        label = "ring3Scale"
    )
    val ring3Alpha by ring3.animateFloat(
        initialValue = 0.7f,
        targetValue = 0f,
        animationSpec = infiniteRepeatable(
            animation = tween(1400, easing = EaseOut, delayMillis = 934),
            repeatMode = RepeatMode.Restart
        ),
        label = "ring3Alpha"
    )

    // Progress bar animation
    val progressAnim = rememberInfiniteTransition(label = "progress")
    val progressOffset by progressAnim.animateFloat(
        initialValue = -1f,
        targetValue = 2f,
        animationSpec = infiniteRepeatable(
            animation = tween(1200, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "progressOffset"
    )

    LaunchedEffect(Unit) {
        startAnimation = true
        delay(2000)
        onFinished()
    }

    Surface(
        modifier = Modifier.fillMaxSize(),
        color = appColors.Background
    ) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            // Content column
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier
                    .alpha(contentAlpha)
                    .scale(contentScale)
            ) {
                // Icon area with pulse rings
                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier.size(200.dp)
                ) {
                    // Ring 3 (outermost)
                    Box(
                        modifier = Modifier
                            .size(160.dp)
                            .scale(ring3Scale)
                            .alpha(ring3Alpha)
                            .drawBehind {
                                drawCircle(
                                    color = appColors.Primary.copy(alpha = 0.35f),
                                    style = Stroke(width = 2.dp.toPx())
                                )
                            }
                    )

                    // Ring 2 (middle)
                    Box(
                        modifier = Modifier
                            .size(160.dp)
                            .scale(ring2Scale)
                            .alpha(ring2Alpha)
                            .drawBehind {
                                drawCircle(
                                    color = appColors.Primary.copy(alpha = 0.5f),
                                    style = Stroke(width = 2.5.dp.toPx())
                                )
                            }
                    )

                    // Ring 1 (innermost pulse)
                    Box(
                        modifier = Modifier
                            .size(160.dp)
                            .scale(ring1Scale)
                            .alpha(ring1Alpha)
                            .drawBehind {
                                drawCircle(
                                    color = appColors.Primary.copy(alpha = 0.65f),
                                    style = Stroke(width = 3.dp.toPx())
                                )
                            }
                    )

                    // Central icon circle with gradient border
                    Box(
                        contentAlignment = Alignment.Center,
                        modifier = Modifier
                            .size(112.dp)
                            .drawBehind {
                                drawCircle(
                                    brush = Brush.sweepGradient(
                                        colors = listOf(
                                            appColors.Primary,
                                            appColors.Accent,
                                            appColors.Primary
                                        )
                                    ),
                                    style = Stroke(width = 3.dp.toPx())
                                )
                            }
                            .background(
                                brush = Brush.radialGradient(
                                    colors = listOf(
                                        appColors.Primary.copy(alpha = 0.18f),
                                        appColors.Accent.copy(alpha = 0.08f),
                                        Color.Transparent
                                    )
                                ),
                                shape = CircleShape
                            )
                    ) {
                        Text(
                            text = "NFC",
                            style = MaterialTheme.typography.headlineMedium.copy(
                                fontWeight = FontWeight.Bold,
                                brush = Brush.linearGradient(
                                    colors = listOf(appColors.Primary, appColors.Accent)
                                )
                            ),
                            fontSize = 28.sp
                        )
                    }
                }

                Spacer(modifier = Modifier.height(28.dp))

                Text(
                    text = stringResource(R.string.app_name),
                    style = MaterialTheme.typography.headlineMedium.copy(
                        fontWeight = FontWeight.Bold
                    ),
                    color = appColors.Primary
                )

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = stringResource(R.string.tagline),
                    style = MaterialTheme.typography.bodyMedium,
                    color = appColors.Secondary,
                    fontSize = 14.sp
                )
            }

            // Animated progress bar at the bottom
            Box(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .fillMaxWidth()
                    .padding(bottom = 48.dp, start = 48.dp, end = 48.dp)
                    .alpha(contentAlpha)
            ) {
                LinearProgressIndicator(
                    progress = { (progressOffset.coerceIn(0f, 1f)) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(2.dp),
                    color = appColors.Primary,
                    trackColor = appColors.Primary.copy(alpha = 0.15f),
                    strokeCap = StrokeCap.Round
                )
            }
        }
    }
}
