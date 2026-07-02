package com.example.bluetoothchattingsystem.ui.screens

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.bluetoothchattingsystem.theme.IceLatte
import com.example.bluetoothchattingsystem.theme.LatteDark
import com.example.bluetoothchattingsystem.theme.NearBlack
import com.example.bluetoothchattingsystem.theme.TheMint
import kotlinx.coroutines.delay

@Composable
fun SplashScreen(
    onTimeout: () -> Unit,
    modifier: Modifier = Modifier
) {
    // Auto-advance to onboarding after 3.5 seconds
    LaunchedEffect(Unit) {
        delay(3500)
        onTimeout()
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(IceLatte)
            .clickable { onTimeout() }, // Or skip on click
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Mimic custom Bluetooth line icon
                Canvas(modifier = Modifier.size(36.dp)) {
                    val w = size.width
                    val h = size.height
                    // Draw simple Bluetooth path
                    val path = androidx.compose.ui.graphics.Path().apply {
                        moveTo(w * 0.5f, h * 0.1f)
                        lineTo(w * 0.5f, h * 0.9f)
                        lineTo(w * 0.75f, h * 0.65f)
                        lineTo(w * 0.25f, h * 0.35f)
                        lineTo(w * 0.75f, h * 0.35f)
                        lineTo(w * 0.5f, h * 0.1f)
                    }
                    drawPath(path, color = TheMint, style = Stroke(width = 4.dp.toPx()))
                }
                Spacer(modifier = Modifier.width(12.dp))
                Text(
                    text = "B-Chat",
                    style = MaterialTheme.typography.titleLarge,
                    color = TheMint,
                    fontSize = 36.sp,
                    fontWeight = FontWeight.ExtraBold
                )
            }
            
            Spacer(modifier = Modifier.height(8.dp))
            
            Text(
                text = "Chat without the internet.",
                style = MaterialTheme.typography.bodyLarge,
                color = NearBlack,
                fontWeight = FontWeight.Medium
            )

            Spacer(modifier = Modifier.height(48.dp))

            // Animated Concentric Pulse
            BluetoothPulse()
        }
    }
}

@Composable
fun BluetoothPulse() {
    val transition = rememberInfiniteTransition(label = "pulse")
    
    val pulse1 by transition.animateFloat(
        initialValue = 0.4f,
        targetValue = 1.3f,
        animationSpec = infiniteRepeatable(
            animation = tween(3000, easing = LinearEasing)
        ),
        label = "pulse1"
    )
    
    val pulse2 by transition.animateFloat(
        initialValue = 0.4f,
        targetValue = 1.3f,
        animationSpec = infiniteRepeatable(
            animation = tween(3000, delayMillis = 1000, easing = LinearEasing)
        ),
        label = "pulse2"
    )

    Box(
        modifier = Modifier.size(160.dp),
        contentAlignment = Alignment.Center
    ) {
        // Pulse 1 ring
        Canvas(modifier = Modifier.fillMaxSize()) {
            val radius = (size.width / 2) * pulse1
            val alpha = (1.0f - (pulse1 - 0.4f) / 0.9f).coerceIn(0.0f, 1.0f)
            drawCircle(
                color = TheMint,
                radius = radius,
                style = Stroke(width = 2.dp.toPx()),
                alpha = alpha * 0.4f
            )
        }

        // Pulse 2 ring
        Canvas(modifier = Modifier.fillMaxSize()) {
            val radius = (size.width / 2) * pulse2
            val alpha = (1.0f - (pulse2 - 0.4f) / 0.9f).coerceIn(0.0f, 1.0f)
            drawCircle(
                color = TheMint,
                radius = radius,
                style = Stroke(width = 2.dp.toPx()),
                alpha = alpha * 0.4f
            )
        }

        // Central Icon Badge (App Logo)
        Box(
            modifier = Modifier
                .size(72.dp)
                .clip(CircleShape)
                .background(Color.White)
                .border(1.5.dp, TheMint, CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Image(
                painter = painterResource(id = com.example.bluetoothchattingsystem.R.drawable.logoo),
                contentDescription = "App Logo",
                modifier = Modifier
                    .size(64.dp)
                    .clip(CircleShape)
            )
        }
    }
}
