package com.example.ui.components

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import com.example.ui.theme.AetherBorder
import com.example.ui.theme.AetherSurface
import com.example.ui.theme.AetherVoid
import com.example.ui.theme.CyanNeon
import com.example.ui.theme.VioletNeon
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.max
import kotlin.math.min
import kotlin.math.sin
import kotlin.random.Random

@Composable
fun CosmicBackground(
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit
) {
    val infiniteTransition = rememberInfiniteTransition(label = "cosmic_shift")
    
    // Aurora shift animation
    val auroraShift by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(20000, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "aurora_shift"
    )

    // Particle system animation
    val particlePhase by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(30000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "particle_phase"
    )

    // Floating orbs animation
    val orbPhase by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(25000, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "orb_phase"
    )

    androidx.compose.foundation.layout.Box(modifier = modifier.fillMaxSize()) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val width = size.width
            val height = size.height
            val centerX = width / 2
            val centerY = height / 2
            val maxDim = max(width, height)

            // Deep void base
            drawRect(color = AetherVoid, topLeft = Offset.Zero, size = size)

            // Animated aurora rings - multiple layers for depth
            val ringCount = 4
            for (i in 0 until ringCount) {
                val progress = (auroraShift + i * 0.25f) % 1f
                val radius = maxDim * (0.3f + 0.7f * progress)
                val alpha = (1f - progress) * 0.12f
                
                val color = when (i % 4) {
                    0 -> VioletNeon.copy(alpha = alpha)
                    1 -> CyanNeon.copy(alpha = alpha)
                    2 -> Color(0xFFEC4899).copy(alpha = alpha)
                    else -> Color(0xFF22D3EE).copy(alpha = alpha)
                }

                drawCircle(
                    color = color,
                    center = Offset(centerX, centerY),
                    radius = radius,
                    style = Stroke(width = 1.5f)
                )
            }

            // Central pulse ring
            val pulseScale = 0.7f + 0.5f * sin(auroraShift * 4 * PI)
            drawCircle(
                color = CyanNeon.copy(alpha = 0.06f),
                center = Offset(centerX, centerY),
                radius = maxDim * 0.3f * pulseScale,
                style = Stroke(width = 2.dp.toPx())
            )

            // Floating orbs - 3D depth effect
            val orbCount = 6
            for (i in 0 until orbCount) {
                val angle = (i * 360f / orbCount + orbPhase * 180f) * (PI / 180)
                val baseRadius = maxDim * 0.2f
                val radiusVariation = maxDim * 0.15f * sin(orbPhase * 2 * PI + i * 1.2f)
                val radius = baseRadius + radiusVariation
                val px = centerX + radius * cos(angle)
                val py = centerY + radius * sin(angle)
                
                // Depth-based size and alpha
                val depth = (sin(orbPhase * PI + i * 0.8f) + 1f) / 2f // 0 to 1
                val orbSize = (8f + 20f * depth).dp.toPx()
                val orbAlpha = 0.08f + 0.22f * depth
                
                val orbColor = when (i % 3) {
                    0 -> VioletNeon.copy(alpha = orbAlpha)
                    1 -> CyanNeon.copy(alpha = orbAlpha)
                    else -> Color(0xFFEC4899).copy(alpha = orbAlpha)
                }

                drawCircle(
                    color = orbColor,
                    center = Offset(px, py),
                    radius = orbSize
                )
            }

            // Particle field - starfield effect
            val particleCount = 80
            val seed = 42
            val random = Random(seed)
            
            for (i in 0 until particleCount) {
                // Deterministic pseudo-random positions
                val baseX = random.nextFloat() * width
                val baseY = random.nextFloat() * height
                val speed = 0.1f + random.nextFloat() * 0.3f
                val sizeVariation = 0.5f + random.nextFloat() * 2.5f
                
                val px = (baseX + sin(particlePhase * speed * 2 * PI + i * 0.5f) * 30f) % width
                val py = (baseY + cos(particlePhase * speed * 2 * PI + i * 0.7f) * 20f) % height
                val particleAlpha = (0.2f + 0.8f * sin(particlePhase * 3 * PI + i * 0.4f)) * 0.4f
                val particleSize = (1f + sizeVariation * sin(particlePhase * 2 * PI + i * 0.9f)).dp.toPx()
                
                val particleColor = when (i % 4) {
                    0 -> CyanNeon.copy(alpha = particleAlpha)
                    1 -> VioletNeon.copy(alpha = particleAlpha)
                    2 -> Color(0xFFEC4899).copy(alpha = particleAlpha)
                    else -> Color.White.copy(alpha = particleAlpha * 0.5f)
                }

                if (px >= 0 && px <= width && py >= 0 && py <= height) {
                    drawCircle(
                        color = particleColor,
                        center = Offset(px, py),
                        radius = max(0.5f, particleSize)
                    )
                }
            }

            // Scanline overlay for retro CRT feel
            val scanlineSpacing = 4.dp.toPx()
            for (y in 0..(height / scanlineSpacing).toInt()) {
                val alpha = (0.005f + 0.01f * sin(particlePhase * 2 * PI + y * 0.1f))
                drawRect(
                    color = Color.White.copy(alpha = alpha),
                    topLeft = Offset(0f, y * scanlineSpacing),
                    size = Size(width, 1.dp.toPx())
                )
            }

            // Vignette corners
            val vignetteRadius = maxDim * 0.8f
            for (i in 0..10) {
                val progress = i / 10f
                val alpha = progress * progress * 0.15f
                drawCircle(
                    color = AetherVoid.copy(alpha = alpha),
                    center = Offset(centerX, centerY),
                    radius = vignetteRadius * (1f - progress * 0.3f)
                )
            }
        }

        content()
    }
}