package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CleaningServices
import androidx.compose.material.icons.filled.GraphicEq
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Public
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.AetherBorder
import com.example.ui.theme.AetherCard
import com.example.ui.theme.CyanNeon
import com.example.ui.theme.StarlightHigh
import com.example.ui.theme.StarlightMedium
import com.example.ui.theme.StarlightMuted
import com.example.ui.theme.VioletNeon

@Composable
fun SettingsScreen(
    onOpenPrivacyDisclosure: () -> Unit,
    onClearHistory: () -> Unit,
    modifier: Modifier = Modifier
) {
    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .testTag("settings_screen"),
        contentPadding = PaddingValues(16.dp)
    ) {
        item {
            Text(
                text = "Settings & Disclosures",
                color = StarlightHigh,
                fontSize = 22.sp,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(16.dp))
        }

        // Privacy Section
        item {
            SettingsCard(
                title = "Privacy & Data Architecture",
                icon = Icons.Default.Lock,
                iconTint = CyanNeon
            ) {
                Text(
                    text = "VYBE is guest-first and local-first. We collect zero analytics, run zero advertising trackers, and require no account. Your playlists, favorites, and history remain strictly on your device.",
                    color = StarlightMedium,
                    fontSize = 13.sp,
                    lineHeight = 18.sp
                )
                Spacer(modifier = Modifier.height(12.dp))
                Button(
                    onClick = onOpenPrivacyDisclosure,
                    colors = ButtonDefaults.buttonColors(containerColor = CyanNeon),
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier.testTag("source_disclosure_btn")
                ) {
                    Text("View Full Disclosure", color = Color.Black, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                }
            }
            Spacer(modifier = Modifier.height(12.dp))
        }

        // Audio Engine Section
        item {
            SettingsCard(
                title = "Audio Engine",
                icon = Icons.Default.GraphicEq,
                iconTint = VioletNeon
            ) {
                Text(
                    text = "Powered by AndroidX Media3 ExoPlayer with hardware audio focus management, background media session, and audio effect processing (Equalizer & Bass Boost).",
                    color = StarlightMedium,
                    fontSize = 13.sp,
                    lineHeight = 18.sp
                )
            }
            Spacer(modifier = Modifier.height(12.dp))
        }

        // Legal Catalogs Section
        item {
            SettingsCard(
                title = "Supported Streaming Providers",
                icon = Icons.Default.Public,
                iconTint = Color(0xFFFBBF24)
            ) {
                Text(
                    text = "• YouTube Music: Millions of tracks via Piped API & yt-dlp extractors\n" +
                            "• Local Audio: On-device MP3, AAC, FLAC, and WAV files",
                    color = StarlightMedium,
                    fontSize = 13.sp,
                    lineHeight = 18.sp
                )
            }
            Spacer(modifier = Modifier.height(12.dp))
        }

        // Storage & Cache Maintenance
        item {
            SettingsCard(
                title = "Data & Storage Management",
                icon = Icons.Default.CleaningServices,
                iconTint = Color(0xFFFF6688)
            ) {
                Text(
                    text = "Clear your on-device recently played listening history whenever you want.",
                    color = StarlightMedium,
                    fontSize = 13.sp
                )
                Spacer(modifier = Modifier.height(10.dp))
                OutlinedButton(
                    onClick = onClearHistory,
                    shape = RoundedCornerShape(8.dp),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = Color(0xFFFF6688)),
                    modifier = Modifier.testTag("clear_history_btn")
                ) {
                    Text("Clear Listening History")
                }
            }
            Spacer(modifier = Modifier.height(12.dp))
        }

        // About & Version
        item {
            SettingsCard(
                title = "About VYBE",
                icon = Icons.Default.Info,
                iconTint = StarlightMedium
            ) {
                Text(
                    text = "VYBE v1.0.0\nNative Kotlin & Jetpack Compose\nMaterial Design 3 • Open Streaming Architecture",
                    color = StarlightMuted,
                    fontSize = 12.sp,
                    lineHeight = 18.sp
                )
            }
        }
    }
}

@Composable
fun SettingsCard(
    title: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    iconTint: Color,
    content: @Composable () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(AetherCard)
            .border(1.dp, AetherBorder, RoundedCornerShape(16.dp))
            .padding(16.dp)
    ) {
        Column {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = iconTint,
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = title,
                    color = StarlightHigh,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.SemiBold
                )
            }
            Spacer(modifier = Modifier.height(10.dp))
            content()
        }
    }
}