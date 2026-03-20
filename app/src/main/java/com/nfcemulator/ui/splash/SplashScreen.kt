package com.nfcemulator.ui.splash

import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.scale
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.nfcemulator.ui.theme.LocalAppColors
import kotlinx.coroutines.delay

@Composable
fun SplashScreen(onFinished: () -> Unit) {
    var startAnimation by remember { mutableStateOf(false) }

    val alphaAnim by animateFloatAsState(
        targetValue = if (startAnimation) 1f else 0f,
        animationSpec = tween(durationMillis = 800),
        label = "alpha"
    )

    val scaleAnim by animateFloatAsState(
        targetValue = if (startAnimation) 1f else 0.5f,
        animationSpec = tween(durationMillis = 800, easing = EaseOutBack),
        label = "scale"
    )

    LaunchedEffect(Unit) {
        startAnimation = true
        delay(1500)
        onFinished()
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(LocalAppColors.current.Background),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier
                .alpha(alphaAnim)
                .scale(scaleAnim)
        ) {
            // NFC pulse circle
            Box(
                modifier = Modifier
                    .size(120.dp)
                    .border(3.dp, LocalAppColors.current.Primary, CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    "NFC",
                    style = MaterialTheme.typography.headlineLarge,
                    color = LocalAppColors.current.Primary
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            Text(
                "NFC Emulator",
                style = MaterialTheme.typography.headlineMedium,
                color = LocalAppColors.current.TextPrimary
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                "Read. Clone. Emulate.",
                style = MaterialTheme.typography.bodyMedium,
                color = LocalAppColors.current.Secondary,
                fontSize = 14.sp
            )
        }
    }
}
