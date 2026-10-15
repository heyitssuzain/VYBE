package com.example.ui.components

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode as AnimRepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Equalizer
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.MusicNote
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.PlaylistAdd
import androidx.compose.material.icons.filled.QueueMusic
import androidx.compose.material.icons.filled.Repeat
import androidx.compose.material.icons.filled.RepeatOne
import androidx.compose.material.icons.filled.Shuffle
import androidx.compose.material.icons.filled.SkipNext
import androidx.compose.material.icons.filled.SkipPrevious
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material.icons.outlined.FavoriteBorder
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.data.model.RepeatMode
import com.example.data.model.Song
import com.example.playback.PlayerState
import com.example.ui.theme.AetherBorder
import com.example.ui.theme.AetherCard
import com.example.ui.theme.AetherVoid
import com.example.ui.theme.AmberStarlight
import com.example.ui.theme.CyanNeon
import com.example.ui.theme.MagentaGlow
import com.example.ui.theme.StarlightHigh
import com.example.ui.theme.StarlightMedium
import com.example.ui.theme.StarlightMuted
import com.example.ui.theme.VioletNeon
import java.util.Locale

@Composable
fun FullPlayerSheet(
    playerState: PlayerState,
    onCollapse: () -> Unit,
    onTogglePlayPause: () -> Unit,
    onSkipNext: () -> Unit,
    onSkipPrevious: () -> Unit,
    onSeekTo: (Long) -> Unit,
    onToggleRepeat: () -> Unit,
    onToggleShuffle: () -> Unit,
    onToggleFavorite: (Song) -> Unit,
    onOpenQueue: () -> Unit,
    onOpenEqualizer: () -> Unit,
    onOpenSleepTimer: () -> Unit,
    onOpenAddToPlaylist: (Song) -> Unit,
    onSetPlaybackSpeed: (Float) -> Unit,
    modifier: Modifier = Modifier
) {
    val song = playerState.currentSong ?: return

    var isUserSeeking by remember { mutableStateOf(false) }
    var seekPositionSlider by remember { mutableFloatStateOf(0f) }
    var showSpeedMenu by remember { mutableStateOf(false) }

    // Vinyl spinning animation
    val infiniteTransition = rememberInfiniteTransition(label = "player_vinyl_spin")
    val rotation by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(12000, easing = LinearEasing),
            repeatMode = AnimRepeatMode.Restart
        ),
        label = "spin_angle"
    )

    // Pulse ring animation
    val pulse by infiniteTransition.animateFloat(
        initialValue = 0.95f,
        targetValue = 1.05f,
        animationSpec = infiniteRepeatable(
            animation = tween(1500, easing = LinearEasing),
            repeatMode = AnimRepeatMode.Reverse
        ),
        label = "pulse_scale"
    )

    Surface(
        modifier = modifier
            .fillMaxSize()
            .statusBarsPadding()
            .navigationBarsPadding()
            .testTag("full_player_sheet"),
        color = AetherVoid
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 24.dp, vertical = 12.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Top Bar
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                IconButton(
                    onClick = onCollapse,
                    modifier = Modifier.testTag("player_collapse_button")
                ) {
                    Icon(
                        imageVector = Icons.Default.KeyboardArrowDown,
                        contentDescription = "Collapse Player",
                        tint = StarlightHigh,
                        modifier = Modifier.size(32.dp)
                    )
                }

                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = "PLAYING FROM",
                        fontSize = 10.sp,
                        color = StarlightMuted,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.5.sp
                    )
                    SourceBadge(source = song.source)
                }

                IconButton(
                    onClick = onOpenQueue,
                    modifier = Modifier.testTag("player_queue_button")
                ) {
                    Icon(
                        imageVector = Icons.Default.QueueMusic,
                        contentDescription = "Playback Queue",
                        tint = StarlightHigh,
                        modifier = Modifier.size(26.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Centerpiece: Cosmic Vinyl with reactive orbital pulse
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth(),
                contentAlignment = Alignment.Center
            ) {
                // Orbital visualizer rings
                Canvas(
                    modifier = Modifier
                        .size(290.dp)
                ) {
                    val radius = size.minDimension / 2f
                    val center = Offset(size.width / 2f, size.height / 2f)

                    if (playerState.isPlaying) {
                        drawCircle(
                            brush = Brush.radialGradient(
                                colors = listOf(CyanNeon.copy(alpha = 0.25f), Color.Transparent),
                                center = center,
                                radius = radius * pulse
                            ),
                            radius = radius * pulse,
                            center = center
                        )

                        drawCircle(
                            color = VioletNeon.copy(alpha = 0.4f),
                            radius = radius * 0.92f,
                            center = center,
                            style = Stroke(width = 2.dp.toPx())
                        )
                    }
                }

                // Vinyl Disk
                Box(
                    modifier = Modifier
                        .size(240.dp)
                        .shadow(20.dp, CircleShape, spotColor = CyanNeon)
                        .clip(CircleShape)
                        .background(Color(0xFF0A0D18))
                        .border(3.dp, Brush.sweepGradient(listOf(CyanNeon, VioletNeon, MagentaGlow, CyanNeon)), CircleShape)
                        .rotate(if (playerState.isPlaying) rotation else 0f),
                    contentAlignment = Alignment.Center
                ) {
                    // Center Album Artwork
                    Box(
                        modifier = Modifier
                            .size(140.dp)
                            .clip(CircleShape)
                            .background(Color(0xFF161B30))
                            .border(2.dp, CyanNeon.copy(alpha = 0.5f), CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        if (!song.artworkUrl.isNullOrBlank()) {
                            AsyncImage(
                                model = song.artworkUrl,
                                contentDescription = song.title,
                                modifier = Modifier.fillMaxSize(),
                                contentScale = ContentScale.Crop
                            )
                        } else {
                            Icon(
                                imageVector = Icons.Default.MusicNote,
                                contentDescription = null,
                                tint = CyanNeon,
                                modifier = Modifier.size(48.dp)
                            )
                        }
                    }

                    // Center Spindle Hole
                    Box(
                        modifier = Modifier
                            .size(24.dp)
                            .clip(CircleShape)
                            .background(AetherVoid)
                            .border(2.dp, VioletNeon, CircleShape)
                    )
                }
            }

            // Error notice if stream failed
            if (playerState.errorMessage != null) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 8.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .background(Color(0xFF4A1020))
                        .padding(8.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = playerState.errorMessage,
                        color = Color(0xFFFF8888),
                        fontSize = 12.sp,
                        textAlign = TextAlign.Center
                    )
                }
            }

            // Song Info
            Text(
                text = song.title,
                color = StarlightHigh,
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(4.dp))

            Text(
                text = song.artist,
                color = StarlightMedium,
                fontSize = 15.sp,
                textAlign = TextAlign.Center,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )

            if (!song.license.isNullOrBlank()) {
                Text(
                    text = song.license,
                    color = StarlightMuted,
                    fontSize = 11.sp,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(top = 2.dp)
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Time and Slider
            val currentPos = if (isUserSeeking) seekPositionSlider.toLong() else playerState.currentPositionMs
            val totalDur = playerState.durationMs.coerceAtLeast(1L)
            val sliderValue = (currentPos.toFloat() / totalDur.toFloat()).coerceIn(0f, 1f)

            Slider(
                value = sliderValue,
                onValueChange = { frac ->
                    isUserSeeking = true
                    seekPositionSlider = frac * totalDur
                },
                onValueChangeFinished = {
                    onSeekTo(seekPositionSlider.toLong())
                    isUserSeeking = false
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("player_seek_slider"),
                colors = SliderDefaults.colors(
                    thumbColor = CyanNeon,
                    activeTrackColor = CyanNeon,
                    inactiveTrackColor = Color(0xFF232B45)
                )
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = formatMs(currentPos),
                    color = StarlightMuted,
                    fontSize = 12.sp
                )
                Text(
                    text = formatMs(totalDur),
                    color = StarlightMuted,
                    fontSize = 12.sp
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Main Playback Controls Cluster
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Shuffle
                IconButton(
                    onClick = onToggleShuffle,
                    modifier = Modifier.testTag("player_shuffle_button")
                ) {
                    Icon(
                        imageVector = Icons.Default.Shuffle,
                        contentDescription = "Shuffle",
                        tint = if (playerState.isShuffleEnabled) CyanNeon else StarlightMuted,
                        modifier = Modifier.size(24.dp)
                    )
                }

                // Previous
                IconButton(
                    onClick = onSkipPrevious,
                    modifier = Modifier.size(48.dp).testTag("player_prev_button")
                ) {
                    Icon(
                        imageVector = Icons.Default.SkipPrevious,
                        contentDescription = "Previous Track",
                        tint = StarlightHigh,
                        modifier = Modifier.size(36.dp)
                    )
                }

                // Cosmic Play/Pause Orb
                Box(
                    modifier = Modifier
                        .size(72.dp)
                        .clip(CircleShape)
                        .background(
                            Brush.linearGradient(
                                listOf(CyanNeon, VioletNeon)
                            )
                        )
                        .clickable(onClick = onTogglePlayPause)
                        .testTag("player_play_pause_button"),
                    contentAlignment = Alignment.Center
                ) {
                    if (playerState.isBuffering) {
                        CircularProgressIndicator(
                            color = Color.Black,
                            strokeWidth = 3.dp,
                            modifier = Modifier.size(32.dp)
                        )
                    } else {
                        Icon(
                            imageVector = if (playerState.isPlaying) Icons.Default.Pause else Icons.Default.PlayArrow,
                            contentDescription = if (playerState.isPlaying) "Pause" else "Play",
                            tint = Color.Black,
                            modifier = Modifier.size(40.dp)
                        )
                    }
                }

                // Next
                IconButton(
                    onClick = onSkipNext,
                    modifier = Modifier.size(48.dp).testTag("player_next_button")
                ) {
                    Icon(
                        imageVector = Icons.Default.SkipNext,
                        contentDescription = "Next Track",
                        tint = StarlightHigh,
                        modifier = Modifier.size(36.dp)
                    )
                }

                // Repeat Mode
                IconButton(
                    onClick = onToggleRepeat,
                    modifier = Modifier.testTag("player_repeat_button")
                ) {
                    val (icon, tint) = when (playerState.repeatMode) {
                        RepeatMode.OFF -> Pair(Icons.Default.Repeat, StarlightMuted)
                        RepeatMode.ALL -> Pair(Icons.Default.Repeat, CyanNeon)
                        RepeatMode.ONE -> Pair(Icons.Default.RepeatOne, VioletNeon)
                    }
                    Icon(
                        imageVector = icon,
                        contentDescription = "Repeat Mode",
                        tint = tint,
                        modifier = Modifier.size(24.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Secondary Quick Actions: Favorite, Equalizer, Sleep Timer, Speed, Add to Playlist
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(16.dp))
                    .background(AetherCard)
                    .border(1.dp, AetherBorder, RoundedCornerShape(16.dp))
                    .padding(horizontal = 8.dp, vertical = 6.dp),
                horizontalArrangement = Arrangement.SpaceAround,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Favorite
                IconButton(
                    onClick = { onToggleFavorite(song) },
                    modifier = Modifier.testTag("player_fav_button")
                ) {
                    Icon(
                        imageVector = if (song.isFavorite) Icons.Filled.Favorite else Icons.Outlined.FavoriteBorder,
                        contentDescription = "Favorite",
                        tint = if (song.isFavorite) MagentaGlow else StarlightMedium,
                        modifier = Modifier.size(22.dp)
                    )
                }

                // Equalizer
                IconButton(
                    onClick = onOpenEqualizer,
                    modifier = Modifier.testTag("player_eq_button")
                ) {
                    Icon(
                        imageVector = Icons.Default.Equalizer,
                        contentDescription = "Equalizer",
                        tint = CyanNeon,
                        modifier = Modifier.size(22.dp)
                    )
                }

                // Sleep Timer
                IconButton(
                    onClick = onOpenSleepTimer,
                    modifier = Modifier.testTag("player_timer_button")
                ) {
                    Icon(
                        imageVector = Icons.Default.Timer,
                        contentDescription = "Sleep Timer",
                        tint = if (playerState.sleepTimerSeconds != null) AmberStarlight else StarlightMedium,
                        modifier = Modifier.size(22.dp)
                    )
                }

                // Speed Selector
                Box {
                    IconButton(
                        onClick = { showSpeedMenu = true },
                        modifier = Modifier.testTag("player_speed_button")
                    ) {
                        Text(
                            text = "${playerState.playbackSpeed}x",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = StarlightHigh
                        )
                    }

                    DropdownMenu(
                        expanded = showSpeedMenu,
                        onDismissRequest = { showSpeedMenu = false },
                        modifier = Modifier.background(Color(0xFF1E243A))
                    ) {
                        listOf(0.75f, 1.0f, 1.25f, 1.5f, 2.0f).forEach { spd ->
                            DropdownMenuItem(
                                text = { Text("${spd}x", color = StarlightHigh) },
                                onClick = {
                                    showSpeedMenu = false
                                    onSetPlaybackSpeed(spd)
                                }
                            )
                        }
                    }
                }

                // Add to Playlist
                IconButton(
                    onClick = { onOpenAddToPlaylist(song) },
                    modifier = Modifier.testTag("player_add_playlist_button")
                ) {
                    Icon(
                        imageVector = Icons.Default.PlaylistAdd,
                        contentDescription = "Add to Playlist",
                        tint = StarlightMedium,
                        modifier = Modifier.size(22.dp)
                    )
                }
            }
        }
    }
}

fun formatMs(ms: Long): String {
    if (ms <= 0L) return "0:00"
    val totalSec = ms / 1000L
    val min = totalSec / 60L
    val sec = totalSec % 60L
    return String.format(Locale.getDefault(), "%d:%02d", min, sec)
}
