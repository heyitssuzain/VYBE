package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.drawRect
import androidx.compose.ui.unit.dp
import com.example.ui.theme.AetherBorder
import com.example.ui.theme.CyanNeon

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
            .border(1.dp, CyanNeon.copy(alpha = 0.15f), RoundedCornerShape(16.dp))
            .shadow(elevationDp, RoundedCornerShape(16.dp), Color.Black.copy(alpha = 0.3f))
    ) {
        content()
    }
}