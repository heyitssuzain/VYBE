package com.example.ui.components

import androidx.compose.animation.animateContentSize
import androidx.compose.animation.animateFloatAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.MusicNote
import androidx.compose.material.icons.filled.QueueMusic
import androidx.compose.material.icons.outlined.FavoriteBorder
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.data.model.AudioSource
import com.example.data.model.Song
import com.example.ui.theme.AetherBorder
import com.example.ui.theme.AetherCard
import com.example.ui.theme.AetherCardElevated
import com.example.ui.theme.CyanNeon
import com.example.ui.theme.MagentaGlow
import com.example.ui.theme.StarlightHigh
import com.example.ui.theme.StarlightMedium
import com.example.ui.theme.StarlightMuted
import com.example.ui.theme.VioletNeon

@Composable
fun TrackItemView(
    song: Song,
    isPlaying: Boolean,
    isCurrentTrack: Boolean,
    onTrackClick: () -> Unit,
    onFavoriteToggle: () -> Unit,
    onPlayNext: () -> Unit,
    onAddToPlaylist: () -> Unit,
    onDownload: () -> Unit,
    modifier: Modifier = Modifier
) {
    var showMenu by remember { mutableStateOf(false) }
    val progress by remember { mutableStateOf(0f) }
    val isDownloading by remember { mutableStateOf(false) }
    val glowAnimation = animateFloatAsState(
        targetValue = if (isPlaying && isCurrentTrack) 1f else 0f,
        animationSpec = androidx.compose.animation.core.tween(1000)
    )

    Row(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .background(
                if (isCurrentTrack) {
                    AetherCardElevated.copy(alpha = 0.95f)
                } else {
                    AetherCard.copy(alpha = 0.7f)
                }
            )
            .border(
                width = if (isCurrentTrack) 1.5.dp else 1.dp,
                color = if (isCurrentTrack) CyanNeon.copy(alpha = glowAnimation.value) else AetherBorder.copy(alpha = 0.5f),
                shape = RoundedCornerShape(14.dp)
            )
            .clickable(onClick = onTrackClick)
            .padding(horizontal = 14.dp, vertical = 10.dp)
            .testTag("track_item_${song.id}")
            .animateContentSize(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Thumbnail with animated border when playing
        Box(
            modifier = Modifier
                .size(52.dp)
                .clip(RoundedCornerShape(10.dp))
                .background(Color(0xFF1A1F35)),
            contentAlignment = Alignment.Center
        ) {
            if (!song.artworkUrl.isNullOrBlank()) {
                AsyncImage(
                    model = song.artworkUrl,
                    contentDescription = song.title,
                    modifier = Modifier
                        .size(52.dp)
                        .clip(RoundedCornerShape(10.dp)),
                    contentScale = ContentScale.Crop
                )
            } else {
                Icon(
                    imageVector = Icons.Default.MusicNote,
                    contentDescription = null,
                    tint = CyanNeon.copy(alpha = 0.7f),
                    modifier = Modifier.size(26.dp)
                )
            }

            // Playing animation overlay
            if (isPlaying && isCurrentTrack) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .clip(RoundedCornerShape(10.dp))
                        .background(Color.Black.copy(alpha = 0.3f)),
                    contentAlignment = Alignment.Center
                ) {
                    Box(
                        modifier = Modifier
                            .size(28.dp)
                            .clip(androidx.compose.foundation.shape.CircleShape)
                            .background(CyanNeon.copy(alpha = 0.9f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.GraphicEq,
                            contentDescription = "Playing",
                            tint = Color.Black,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }
            }

            // Download progress overlay
            if (isDownloading) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .clip(RoundedCornerShape(10.dp))
                        .background(Color.Black.copy(alpha = 0.7f)),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        androidx.compose.material3.CircularProgressIndicator(
                            color = CyanNeon,
                            modifier = Modifier.size(32.dp),
                            progress = progress
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "${(progress * 100).toInt()}%",
                            color = Color.White,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }

            // Downloaded checkmark
            if (song.isDownloaded && !isDownloading) {
                Box(
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .size(20.dp)
                        .clip(androidx.compose.foundation.shape.CircleShape)
                        .background(CyanNeon.copy(alpha = 0.9f))
                        .padding(4.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Check,
                        contentDescription = "Downloaded",
                        tint = Color.Black,
                        modifier = Modifier.size(14.dp)
                    )
                }
            }
        }

        Spacer(modifier = Modifier.width(12.dp))

        // Title and Artist
        Column(modifier = Modifier.weight(1f).width(0.dp)) {
            Text(
                text = song.title,
                color = if (isCurrentTrack) CyanNeon else StarlightHigh,
                fontSize = 15.sp,
                fontWeight = if (isCurrentTrack) FontWeight.SemiBold else FontWeight.Medium,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )

            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.padding(top = 3.dp)
            ) {
                // Source Badge
                SourceBadge(source = song.source)

                Spacer(modifier = Modifier.width(8.dp))

                Text(
                    text = song.artist,
                    color = StarlightMedium,
                    fontSize = 12.sp,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }

        // Action Buttons Row
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = androidx.compose.foundation.layout.Arrangement.spacedBy(4.dp)
        ) {
            // Favorite Toggle
            IconButton(
                onClick = onFavoriteToggle,
                modifier = Modifier
                    .size(40.dp)
                    .testTag("fav_btn_${song.id}")
            ) {
                Icon(
                    imageVector = if (song.isFavorite) Icons.Filled.Favorite else Icons.Outlined.FavoriteBorder,
                    contentDescription = if (song.isFavorite) "Remove Favorite" else "Add Favorite",
                    tint = if (song.isFavorite) MagentaGlow else StarlightMuted,
                    modifier = Modifier.size(20.dp)
                )
            }

            // Download Button
            IconButton(
                onClick = {
                    if (!song.isDownloaded && !isDownloading) {
                        onDownload()
                    }
                },
                enabled = !song.isDownloaded && !isDownloading,
                modifier = Modifier
                    .size(40.dp)
                    .testTag("download_btn_${song.id}")
            ) {
                Icon(
                    imageVector = if (song.isDownloaded) Icons.Default.Check else Icons.Default.Download,
                    contentDescription = if (song.isDownloaded) "Downloaded" else "Download for Offline",
                    tint = if (song.isDownloaded) CyanNeon else StarlightMedium,
                    modifier = Modifier.size(20.dp)
                )
            }

            // More options dropdown
            Box {
                IconButton(
                    onClick = { showMenu = true },
                    modifier = Modifier
                        .size(40.dp)
                        .testTag("more_btn_${song.id}")
                ) {
                    Icon(
                        imageVector = Icons.Default.MoreVert,
                        contentDescription = "More Options",
                        tint = StarlightMedium,
                        modifier = Modifier.size(20.dp)
                    )
                }

                DropdownMenu(
                    expanded = showMenu,
                    onDismissRequest = { showMenu = false },
                    modifier = Modifier.background(AetherCardElevated)
                ) {
                    DropdownMenuItem(
                        text = { Text("Play Next", color = StarlightHigh) },
                        onClick = {
                            showMenu = false
                            onPlayNext()
                        }
                    )
                    DropdownMenuItem(
                        text = { Text("Add to Playlist", color = StarlightHigh) },
                        onClick = {
                            showMenu = false
                            onAddToPlaylist()
                        }
                    )
                    DropdownMenuItem(
                        text = { Text("Add to Queue", color = StarlightHigh) },
                        onClick = {
                            showMenu = false
                            // TODO: Add to queue
                        }
                    )
                }
            }
        }
    }
}

@Composable
fun SourceBadge(source: AudioSource) {
    val (label, bgColor, textColor) = when (source) {
        AudioSource.LOCAL -> Triple("LOCAL", Color(0xFF1E3A5F), CyanNeon)
        AudioSource.YOUTUBE -> Triple("YT", Color(0xFF38155A), VioletNeon)
    }

    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(4.dp))
            .background(bgColor)
            .padding(horizontal = 6.dp, vertical = 1.dp)
    ) {
        Text(
            text = label,
            color = textColor,
            fontSize = 9.sp,
            fontWeight = FontWeight.Bold
        )
    }
}