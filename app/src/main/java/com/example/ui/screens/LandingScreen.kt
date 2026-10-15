package com.example.ui.screens

import androidx.compose.animation.animateFloatAsState
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.MusicNote
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.draw.scale
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Canvas
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.drawCircle
import androidx.compose.ui.graphics.drawscope.drawRect
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.ui.components.GlassCard
import com.example.ui.theme.AetherBorder
import com.example.ui.theme.AetherCard
import com.example.ui.theme.AetherVoid
import com.example.ui.theme.CyanNeon
import com.example.ui.theme.StarlightHigh
import com.example.ui.theme.StarlightMedium
import com.example.ui.theme.StarlightMuted
import com.example.ui.theme.VioletNeon
import kotlin.math.cos
import kotlin.math.max
import kotlin.math.min
import kotlin.math.sin
import kotlin.random.Random

@Composable
fun LandingScreen(
    onGetStarted: () -> Unit,
    modifier: Modifier = Modifier
) {
    val infiniteTransition = rememberInfiniteTransition(label = "landing_bg")
    
    // Aurora shift animation
    val auroraShift by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(15000, easing = androidx.compose.animation.core.LinearEasing),
            repeatMode = androidx.compose.animation.core.RepeatMode.Reverse
        ),
        label = "aurora_shift"
    )

    // Particle system animation
    val particlePhase by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(20000, easing = androidx.compose.animation.core.LinearEasing),
            repeatMode = androidx.compose.animation.core.RepeatMode.Restart
        ),
        label = "particle_phase"
    )

    // Entrance animations
    val logoScale by animateFloatAsState(
        targetValue = 1f,
        animationSpec = tween(1200, delayMillis = 300, easing = androidx.compose.animation.core.Easing.OutExpo)
    )
    val logoRotation by animateFloatAsState(
        targetValue = 0f,
        animationSpec = tween(1200, delayMillis = 300, easing = androidx.compose.animation.core.Easing.OutExpo)
    )
    val titleAlpha by animateFloatAsState(
        targetValue = 1f,
        animationSpec = tween(800, delayMillis = 800)
    )
    val subtitleAlpha by animateFloatAsState(
        targetValue = 1f,
        animationSpec = tween(800, delayMillis = 1000)
    )
    val featuresAlpha by animateFloatAsState(
        targetValue = 1f,
        animationSpec = tween(800, delayMillis = 1200)
    )
    val buttonAlpha by animateFloatAsState(
        targetValue = 1f,
        animationSpec = tween(800, delayMillis = 1400)
    )

    Box(
        modifier = modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        // Cosmic Background Canvas
        Canvas(modifier = Modifier.fillMaxSize()) {
            val width = size.width
            val height = size.height
            val centerX = width / 2
            val centerY = height / 2

            // Deep void base
            drawRect(
                color = AetherVoid,
                topLeft = Offset.Zero,
                size = size
            )

            // Animated aurora rings
            val ringCount = 3
            for (i in 0 until ringCount) {
                val progress = (auroraShift + i * 0.33f) % 1f
                val radius = max(width, height) * (0.4f + 0.6f * progress)
                val alpha = (1f - progress) * 0.15f
                val hue = (i * 120 + auroraShift * 60) % 360
                
                val color = when (i % 3) {
                    0 -> VioletNeon.copy(alpha = alpha)
                    1 -> CyanNeon.copy(alpha = alpha)
                    else -> Color(0xFFEC4899).copy(alpha = alpha)
                }

                drawCircle(
                    color = color,
                    center = Offset(centerX, centerY),
                    radius = radius,
                    style = androidx.compose.ui.graphics.drawscope.Stroke(width = 2.dp.toPx())
                )
            }

            // Floating particles
            val particleCount = 50
            for (i in 0 until particleCount) {
                val angle = (i * 360f / particleCount + particlePhase * 360f) * (kotlin.math.PI / 180)
                val radius = min(width, height) * (0.15f + 0.35f * sin(particlePhase * 2 * kotlin.math.PI + i * 0.5f))
                val px = centerX + radius * cos(angle)
                val py = centerY + radius * sin(angle)
                val particleAlpha = (0.3f + 0.7f * sin(particlePhase * 4 * kotlin.math.PI + i * 0.3f)) * 0.5f
                val particleSize = (2f + 4f * sin(particlePhase * 3 * kotlin.math.PI + i * 0.7f)).dp.toPx()
                
                drawCircle(
                    color = CyanNeon.copy(alpha = particleAlpha),
                    center = Offset(px, py),
                    radius = particleSize
                )
            }

            // Central pulse ring
            val pulseScale = 0.8f + 0.4f * sin(auroraShift * 4 * kotlin.math.PI)
            drawCircle(
                color = CyanNeon.copy(alpha = 0.08f),
                center = Offset(centerX, centerY),
                radius = min(width, height) * 0.25f * pulseScale,
                style = androidx.compose.ui.graphics.drawscope.Stroke(width = 3.dp.toPx())
            )

            // Scanline overlay for retro feel
            val scanlineSpacing = 4.dp.toPx()
            for (y in 0..(height / scanlineSpacing).toInt()) {
                drawRect(
                    color = Color.White.copy(alpha = 0.01f),
                    topLeft = Offset(0f, y * scanlineSpacing),
                    size = androidx.compose.ui.geometry.Size(width, 1.dp.toPx())
                )
            }
        }

        // Vignette overlay
        Box(
            modifier = Modifier
                .fillMaxSize()
                .drawBehind {
                    val width = size.width.toFloat()
                    val height = size.height.toFloat()
                    drawRect(
                        brush = Brush.radialGradient(
                            colors = listOf(
                                Color.Transparent,
                                Color.Transparent,
                                AetherVoid.copy(alpha = 0.3f),
                                AetherVoid.copy(alpha = 0.7f),
                                AetherVoid
                            ),
                            center = Offset(width / 2, height / 2),
                            radius = max(width, height) * 0.7f
                        )
                    )
                }
        )

        // Content
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 24.dp, vertical = 32.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = androidx.compose.foundation.layout.Arrangement.Center
        ) {
            Spacer(modifier = Modifier.weight(1f))

            // Logo with 3D rotation effect
            Box(
                modifier = Modifier
                    .size(140.dp)
                    .graphicsLayer {
                        scaleX = logoScale
                        scaleY = logoScale
                        rotationZ = logoRotation * 15f
                        cameraDistance = 10000f
                    }
                    .testTag("landing_logo")
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .clip(RoundedCornerShape(32.dp))
                        .background(
                            Brush.linearGradient(
                                colors = listOf(
                                    Color(0xFF0F1428),
                                    Color(0xFF1A1F3A),
                                    Color(0xFF0D1226)
                                ),
                                start = androidx.compose.ui.geometry.Offset(0f, 0f),
                                end = androidx.compose.ui.geometry.Offset(size.width, size.height)
                            )
                        )
                        .border(2.dp, CyanNeon.copy(alpha = 0.6f), RoundedCornerShape(32.dp))
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(16.dp)
                            .clip(RoundedCornerShape(20.dp))
                            .background(
                                Brush.radialGradient(
                                    colors = listOf(
                                        CyanNeon.copy(alpha = 0.3f),
                                        VioletNeon.copy(alpha = 0.15f),
                                        Color.Transparent
                                    ),
                                    center = androidx.compose.ui.geometry.Offset(size.width / 2, size.height / 2),
                                    radius = max(size.width, size.height) / 2
                                )
                            )
                    ) {
                        Icon(
                            imageVector = Icons.Default.MusicNote,
                            contentDescription = "VYBE Logo",
                            tint = Brush.horizontalGradient(
                                colors = listOf(CyanNeon, VioletNeon, Color(0xFFEC4899))
                            ),
                            modifier = Modifier
                                .fillMaxSize()
                                .wrapContentSize(Alignment.Center)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(28.dp))

            // App Name with gradient text
            Text(
                text = "VYBE",
                color = Brush.horizontalGradient(
                    colors = listOf(CyanNeon, VioletNeon, Color(0xFFEC4899), CyanNeon)
                ),
                fontSize = 56.sp,
                fontWeight = FontWeight.ExtraBold,
                letterSpacing = 8.sp,
                modifier = Modifier
                    .graphicsLayer { alpha = titleAlpha }
                    .testTag("landing_title")
            )

            Spacer(modifier = Modifier.height(8.dp))

            // Tagline
            Text(
                text = "Feel the Music. Own the Moment.",
                color = StarlightMedium.copy(alpha = subtitleAlpha),
                fontSize = 16.sp,
                letterSpacing = 2.sp,
                modifier = Modifier
                    .testTag("landing_subtitle")
            )

            Spacer(modifier = Modifier.height(48.dp))

            // Feature highlights
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .graphicsLayer { alpha = featuresAlpha },
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                FeatureRow(
                    icon = Icons.Default.PlayArrow,
                    title = "Instant YouTube Search",
                    description = "Search millions of tracks with official HD artwork"
                )
                Spacer(modifier = Modifier.height(12.dp))
                FeatureRow(
                    icon = Icons.Default.Download,
                    title = "Offline Downloads",
                    description = "Download any track for offline listening"
                )
                Spacer(modifier = Modifier.height(12.dp))
                FeatureRow(
                    icon = Icons.Default.GraphicEq,
                    title = "Lossless Quality",
                    description = "M4A/AAC streams via Piped & yt-dlp extractors"
                )
                Spacer(modifier = Modifier.height(12.dp))
                FeatureRow(
                    icon = Icons.Default.Security,
                    title = "Zero Tracking",
                    description = "No accounts, no ads, complete privacy"
                )
            }

            Spacer(modifier = Modifier.height(56.dp))

            // Get Started Button
            Button(
                onClick = onGetStarted,
                modifier = Modifier
                    .fillMaxWidth()
                    .width(280.dp)
                    .graphicsLayer { alpha = buttonAlpha }
                    .testTag("landing_get_started"),
                colors = ButtonDefaults.buttonColors(
                    containerColor = CyanNeon,
                    contentColor = Color.Black
                ),
                shape = RoundedCornerShape(16.dp),
                elevation = ButtonDefaults.elevatedButtonElevation(
                    defaultElevation = 8.dp,
                    pressedElevation = 12.dp
                )
            ) {
                Row(
                    modifier = Modifier.padding(vertical = 16.dp),
                    horizontalArrangement = androidx.compose.foundation.layout.Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Start Your VYBE",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.sp
                    )
                    Spacer(modifier = Modifier.width(10.dp))
                    Icon(
                        imageVector = Icons.Default.PlayArrow,
                        contentDescription = null,
                        tint = Color.Black,
                        modifier = Modifier.size(22.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.weight(1f))

            // Bottom credits
            Text(
                text = "Powered by YouTube • Piped API • yt-dlp",
                color = StarlightMuted.copy(alpha = 0.6f),
                fontSize = 11.sp,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(bottom = 24.dp)
            )
        }
    }
}

@Composable
fun FeatureRow(
    icon: androidx.compose.material.icons.Icons.Filled.MusicNote,
    title: String,
    description: String
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier.size(44.dp),
            contentAlignment = Alignment.Center
        ) {
            Box(
                modifier = Modifier
                    .size(44.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(
                        Brush.linearGradient(
                            colors = listOf(
                                Color(0xFF1A1F3A),
                                Color(0xFF0F1428)
                            )
                        )
                    )
                    .border(1.dp, CyanNeon.copy(alpha = 0.3f), RoundedCornerShape(12.dp))
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = CyanNeon,
                    modifier = Modifier.size(22.dp)
                )
            }
        }

        Spacer(modifier = Modifier.width(16.dp))

        Column(
            modifier = Modifier.weight(1f),
            horizontalAlignment = Alignment.Start
        ) {
            Text(
                text = title,
                color = StarlightHigh,
                fontSize = 14.sp,
                fontWeight = FontWeight.SemiBold
            )
            Text(
                text = description,
                color = StarlightMuted,
                fontSize = 12.sp
            )
        }
    }
}

@Composable
fun GlassCard(
    modifier: Modifier = Modifier,
    elevation: Int = 1,
    content: @Composable () -> Unit
) {
    val elevationDp = when (elevation) {
        1 -> 4.dp
        2 -> 8.dp
        3 -> 16.dp
        else -> 4.dp
    }

    Box(
        modifier = modifier
            .clip(RoundedCornerShape(16.dp))
            .background(
                Brush.linearGradient(
                    colors = listOf(
                        Color(0xFF1A1F3A).copy(alpha = 0.85f),
                        Color(0xFF0F1428).copy(alpha = 0.95f)
                    ),
                    start = androidx.compose.ui.geometry.Offset(0f, 0f),
                    end = androidx.compose.ui.geometry.Offset(0f, size.height)
                )
            )
            .border(1.dp, com.example.ui.theme.CyanNeon.copy(alpha = 0.15f), RoundedCornerShape(16.dp))
            .shadow(elevationDp, RoundedCornerShape(16.dp), Color.Black.copy(alpha = 0.3f))
    ) {
        content()
    }
}