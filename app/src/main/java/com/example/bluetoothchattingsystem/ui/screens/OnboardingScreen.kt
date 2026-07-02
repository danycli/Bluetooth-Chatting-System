package com.example.bluetoothchattingsystem.ui.screens

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.with
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.bluetoothchattingsystem.theme.IceLatte
import com.example.bluetoothchattingsystem.theme.LatteDark
import com.example.bluetoothchattingsystem.theme.NearBlack
import com.example.bluetoothchattingsystem.theme.TheMint

@OptIn(ExperimentalAnimationApi::class)
@Composable
fun OnboardingScreen(
    onFinished: () -> Unit,
    modifier: Modifier = Modifier
) {
    var currentSlide by remember { mutableStateOf(1) }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(IceLatte)
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.SpaceBetween
    ) {
        // Skip Button Header
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.End
        ) {
            if (currentSlide < 3) {
                TextButton(onClick = onFinished) {
                    Text(
                        text = "Skip",
                        color = LatteDark,
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp
                    )
                }
            } else {
                Spacer(modifier = Modifier.height(36.dp)) // Maintain spacing height
            }
        }

        // Slide Content with animated transitions
        Box(
            modifier = Modifier.weight(1f),
            contentAlignment = Alignment.Center
        ) {
            AnimatedContent(
                targetState = currentSlide,
                transitionSpec = {
                    fadeIn(animationSpec = tween(300)) with fadeOut(animationSpec = tween(300))
                },
                label = "carousel"
            ) { slide ->
                when (slide) {
                    1 -> OnboardingSlide(
                        title = "No Wi-Fi? No Problem.",
                        description = "Connect and chat with nearby devices directly over Bluetooth. Absolutely zero network or internet required.",
                        illustration = { WifiFreeIllustration() }
                    )
                    2 -> OnboardingSlide(
                        title = "Find People Nearby",
                        description = "Scan and instantly discover nearby users in emergency zones, campsites, flights, or off-grid areas.",
                        illustration = { ScanDiscoveryIllustration() }
                    )
                    3 -> OnboardingSlide(
                        title = "Local & Private",
                        description = "Your communications are stored on your device only. Safe from tracking, data leaks, and central database failures.",
                        illustration = { LocalPrivacyIllustration() }
                    )
                }
            }
        }

        // Pagination and Navigation Buttons
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(24.dp),
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 16.dp)
        ) {
            // Pagination Dots
            Row(
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                for (i in 1..3) {
                    val isActive = i == currentSlide
                    val width = if (isActive) 24.dp else 8.dp
                    val color = if (isActive) TheMint else LatteDark
                    Box(
                        modifier = Modifier
                            .padding(horizontal = 4.dp)
                            .size(height = 8.dp, width = width)
                            .clip(CircleShape)
                            .background(color)
                    )
                }
            }

            // Primary Navigation Button
            Button(
                onClick = {
                    if (currentSlide < 3) {
                        currentSlide++
                    } else {
                        onFinished()
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = TheMint,
                    contentColor = Color.White
                )
            ) {
                Text(
                    text = if (currentSlide == 3) "Get Started" else "Next",
                    style = MaterialTheme.typography.labelLarge,
                    fontSize = 16.sp
                )
            }
        }
    }
}

@Composable
fun OnboardingSlide(
    title: String,
    description: String,
    illustration: @Composable () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
        modifier = modifier.fillMaxWidth()
    ) {
        Box(
            modifier = Modifier
                .height(180.dp)
                .fillMaxWidth(),
            contentAlignment = Alignment.Center
        ) {
            illustration()
        }
        
        Spacer(modifier = Modifier.height(32.dp))
        
        Text(
            text = title,
            style = MaterialTheme.typography.titleLarge,
            color = NearBlack,
            fontSize = 24.sp,
            fontWeight = FontWeight.Bold,
            textAlign = TextAlign.Center
        )
        
        Spacer(modifier = Modifier.height(12.dp))
        
        Text(
            text = description,
            style = MaterialTheme.typography.bodyLarge,
            color = NearBlack,
            fontSize = 15.sp,
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(horizontal = 16.dp),
            lineHeight = 22.sp
        )
    }
}

// Wi-Fi signal radiating between devices
@Composable
fun WifiFreeIllustration() {
    Canvas(modifier = Modifier.size(160.dp, 120.dp)) {
        val w = size.width
        val h = size.height

        // Draw left device
        drawRoundRect(
            color = LatteDark,
            topLeft = Offset(w * 0.15f, h * 0.15f),
            size = androidx.compose.ui.geometry.Size(32.dp.toPx(), 70.dp.toPx()),
            cornerRadius = androidx.compose.ui.geometry.CornerRadius(6.dp.toPx()),
            style = Stroke(width = 3.dp.toPx())
        )

        // Draw right device
        drawRoundRect(
            color = LatteDark,
            topLeft = Offset(w * 0.65f, h * 0.15f),
            size = androidx.compose.ui.geometry.Size(32.dp.toPx(), 70.dp.toPx()),
            cornerRadius = androidx.compose.ui.geometry.CornerRadius(6.dp.toPx()),
            style = Stroke(width = 3.dp.toPx())
        )

        // Draw concentric radiating waves in center
        val center = Offset(w * 0.5f, h * 0.45f)
        drawCircle(
            color = TheMint,
            center = center,
            radius = 12.dp.toPx(),
            style = Stroke(width = 2.dp.toPx(), pathEffect = androidx.compose.ui.graphics.PathEffect.dashPathEffect(floatArrayOf(5f, 5f)))
        )
        drawCircle(
            color = TheMint,
            center = center,
            radius = 24.dp.toPx(),
            style = Stroke(width = 2.dp.toPx()),
            alpha = 0.6f
        )
        drawCircle(
            color = TheMint,
            center = center,
            radius = 36.dp.toPx(),
            style = Stroke(width = 1.5f.dp.toPx()),
            alpha = 0.3f
        )
    }
}

// Radar scan lines discover circles
@Composable
fun ScanDiscoveryIllustration() {
    Canvas(modifier = Modifier.size(160.dp, 120.dp)) {
        val w = size.width
        val h = size.height
        val center = Offset(w * 0.5f, h * 0.5f)

        // Radar circles
        drawCircle(color = TheMint, center = center, radius = 45.dp.toPx(), style = Stroke(width = 1.dp.toPx(), pathEffect = androidx.compose.ui.graphics.PathEffect.dashPathEffect(floatArrayOf(10f, 10f))))
        drawCircle(color = TheMint, center = center, radius = 25.dp.toPx(), style = Stroke(width = 1.dp.toPx()))
        drawCircle(color = TheMint, center = center, radius = 5.dp.toPx())

        // Discovered peers
        drawCircle(color = TheMint, center = Offset(w * 0.3f, h * 0.25f), radius = 10.dp.toPx())
        drawCircle(color = TheMint, center = Offset(w * 0.7f, h * 0.75f), radius = 10.dp.toPx())
    }
}

// Local safety vault check lock inside phone
@Composable
fun LocalPrivacyIllustration() {
    Canvas(modifier = Modifier.size(160.dp, 120.dp)) {
        val w = size.width
        val h = size.height

        // Draw phone base
        drawRoundRect(
            color = LatteDark,
            topLeft = Offset(w * 0.35f, h * 0.15f),
            size = androidx.compose.ui.geometry.Size(48.dp.toPx(), 78.dp.toPx()),
            cornerRadius = androidx.compose.ui.geometry.CornerRadius(8.dp.toPx()),
            style = Stroke(width = 3.dp.toPx())
        )

        // Draw lock body
        val lockCenter = Offset(w * 0.5f, h * 0.52f)
        drawCircle(color = TheMint, center = lockCenter, radius = 15.dp.toPx())

        // Draw checkmark inside lock
        val path = androidx.compose.ui.graphics.Path().apply {
            moveTo(w * 0.46f, h * 0.52f)
            lineTo(w * 0.49f, h * 0.55f)
            lineTo(w * 0.55f, h * 0.47f)
        }
        drawPath(path, color = Color.White, style = Stroke(width = 3.dp.toPx()))
    }
}
