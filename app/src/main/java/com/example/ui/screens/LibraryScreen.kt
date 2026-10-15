package com.example.ui.screens

import android.Manifest
import android.net.Uri
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
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
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.FileOpen
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material.icons.filled.MusicNote
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.TabRowDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.Song
import com.example.playback.PlayerState
import com.example.ui.LibrarySubTab
import com.example.ui.components.TrackItemView
import com.example.ui.theme.AetherBorder
import com.example.ui.theme.AetherCard
import com.example.ui.theme.CyanNeon
import com.example.ui.theme.StarlightHigh
import com.example.ui.theme.StarlightMedium
import com.example.ui.theme.StarlightMuted
import com.example.ui.theme.VioletNeon

@Composable
fun LibraryScreen(
    subTab: LibrarySubTab,
    onSubTabSelect: (LibrarySubTab) -> Unit,
    localTracks: List<Song>,
    isScanning: Boolean,
    playerState: PlayerState,
    onScanClick: () -> Unit,
    onImportSafUri: (Uri) -> Unit,
    onSongClick: (Song, List<Song>) -> Unit,
    onToggleFavorite: (Song) -> Unit,
    onPlayNext: (Song) -> Unit,
    onAddToPlaylist: (Song) -> Unit,
    onDownload: (Song) -> Unit,
    onRemoveDownload: (Song) -> Unit,
    modifier: Modifier = Modifier
) {
    // Media permission launcher
    val permLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            onScanClick()
        }
    }

    // SAF file picker launcher (Open Audio)
    val safPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocument()
    ) { uri: Uri? ->
        if (uri != null) {
            onImportSafUri(uri)
        }
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .testTag("library_screen")
    ) {
        // Top Action Bar
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 10.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Button(
                onClick = {
                    val perm = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                        Manifest.permission.READ_MEDIA_AUDIO
                    } else {
                        Manifest.permission.READ_EXTERNAL_STORAGE
                    }
                    permLauncher.launch(perm)
                },
                colors = ButtonDefaults.buttonColors(containerColor = CyanNeon),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier
                    .weight(1f)
                    .testTag("scan_library_button")
            ) {
                Icon(imageVector = Icons.Default.Refresh, contentDescription = null, tint = Color.Black)
                Spacer(modifier = Modifier.width(6.dp))
                Text("Scan Storage", color = Color.Black, fontWeight = FontWeight.Bold, fontSize = 12.sp)
            }

            OutlinedButton(
                onClick = {
                    safPickerLauncher.launch(arrayOf("audio/*"))
                },
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier
                    .weight(1f)
                    .testTag("import_saf_button")
            ) {
                Icon(imageVector = Icons.Default.FileOpen, contentDescription = null, tint = VioletNeon)
                Spacer(modifier = Modifier.width(6.dp))
                Text("Import Audio", color = StarlightHigh, fontSize = 12.sp)
            }
        }

        // Sub Tabs: Tracks, Artists, Albums
        val tabs = listOf(
            LibrarySubTab.TRACKS to "All Tracks",
            LibrarySubTab.ARTISTS to "Artists",
            LibrarySubTab.ALBUMS to "Albums"
        )
        val selectedIndex = tabs.indexOfFirst { it.first == subTab }.coerceAtLeast(0)

        TabRow(
            selectedTabIndex = selectedIndex,
            containerColor = Color.Transparent,
            contentColor = CyanNeon,
            indicator = { tabPositions ->
                TabRowDefaults.SecondaryIndicator(
                    modifier = Modifier.tabIndicatorOffset(tabPositions[selectedIndex]),
                    color = CyanNeon
                )
            }
        ) {
            tabs.forEachIndexed { index, (tab, label) ->
                Tab(
                    selected = selectedIndex == index,
                    onClick = { onSubTabSelect(tab) },
                    text = {
                        Text(
                            text = label,
                            color = if (selectedIndex == index) CyanNeon else StarlightMuted,
                            fontSize = 13.sp,
                            fontWeight = if (selectedIndex == index) FontWeight.Bold else FontWeight.Normal
                        )
                    }
                )
            }
        }

        if (isScanning) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .weight(1f),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    CircularProgressIndicator(color = CyanNeon)
                    Spacer(modifier = Modifier.height(12.dp))
                    Text("Scanning on-device music files...", color = StarlightMedium)
                }
            }
        } else if (localTracks.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .weight(1f)
                    .padding(32.dp),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(
                        imageVector = Icons.Default.MusicNote,
                        contentDescription = null,
                        tint = StarlightMuted,
                        modifier = Modifier.size(56.dp)
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        text = "No local tracks detected on this device.\nTap 'Scan Storage' to grant media access, or use 'Import Audio' to choose an MP3/FLAC directly.",
                        color = StarlightMuted,
                        fontSize = 13.sp,
                        textAlign = TextAlign.Center
                    )
                }
            }
        } else {
            when (subTab) {
                LibrarySubTab.TRACKS, LibrarySubTab.FOLDERS -> {
                    LazyColumn(
                        modifier = Modifier
                            .fillMaxSize()
                            .weight(1f),
                        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 8.dp)
                    ) {
                        items(localTracks) { song ->
                            TrackItemView(
                                song = song,
                                isPlaying = playerState.isPlaying && playerState.currentSong?.id == song.id,
                                isCurrentTrack = playerState.currentSong?.id == song.id,
                                onTrackClick = { onSongClick(song, localTracks) },
                                onFavoriteToggle = { onToggleFavorite(song) },
                                onPlayNext = { onPlayNext(song) },
                                onAddToPlaylist = { onAddToPlaylist(song) },
                                onDownload = { 
                                    if (song.isDownloaded) {
                                        onRemoveDownload(song)
                                    } else {
                                        onDownload(song)
                                    }
                                },
                                modifier = Modifier.padding(vertical = 2.dp)
                            )
                        }
                    }
                }
                LibrarySubTab.ARTISTS -> {
                    val grouped = localTracks.groupBy { it.artist }
                    LazyColumn(
                        modifier = Modifier
                            .fillMaxSize()
                            .weight(1f),
                        contentPadding = PaddingValues(16.dp)
                    ) {
                        items(grouped.keys.toList()) { artist ->
                            val tracks = grouped[artist] ?: emptyList()
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 6.dp)
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(AetherCard)
                                    .clickable { onSongClick(tracks.first(), tracks) }
                                    .padding(14.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(imageVector = Icons.Default.Person, contentDescription = null, tint = VioletNeon)
                                Spacer(modifier = Modifier.width(12.dp))
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(artist, color = StarlightHigh, fontSize = 15.sp, fontWeight = FontWeight.SemiBold)
                                    Text("${tracks.size} tracks", color = StarlightMuted, fontSize = 12.sp)
                                }
                            }
                        }
                    }
                }
                LibrarySubTab.ALBUMS -> {
                    val grouped = localTracks.groupBy { if (it.album.isBlank()) "Unknown Album" else it.album }
                    LazyColumn(
                        modifier = Modifier
                            .fillMaxSize()
                            .weight(1f),
                        contentPadding = PaddingValues(16.dp)
                    ) {
                        items(grouped.keys.toList()) { album ->
                            val tracks = grouped[album] ?: emptyList()
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 6.dp)
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(AetherCard)
                                    .clickable { onSongClick(tracks.first(), tracks) }
                                    .padding(14.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(imageVector = Icons.Default.Folder, contentDescription = null, tint = CyanNeon)
                                Spacer(modifier = Modifier.width(12.dp))
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(album, color = StarlightHigh, fontSize = 15.sp, fontWeight = FontWeight.SemiBold)
                                    Text("${tracks.size} tracks", color = StarlightMuted, fontSize = 12.sp)
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}