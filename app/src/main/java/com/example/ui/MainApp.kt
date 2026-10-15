package com.example.ui

import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.LibraryMusic
import androidx.compose.material.icons.filled.QueueMusic
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.components.AddToPlaylistDialog
import com.example.ui.components.CosmicBackground
import com.example.ui.components.EqualizerDialog
import com.example.ui.components.FullPlayerSheet
import com.example.ui.components.GlassCard
import com.example.ui.components.MiniPlayer
import com.example.ui.components.PrivacyDisclosureDialog
import com.example.ui.components.QueueSheet
import com.example.ui.components.SleepTimerDialog
import com.example.ui.screens.HomeScreen
import com.example.ui.screens.LandingScreen
import com.example.ui.screens.LibraryScreen
import com.example.ui.screens.OnboardingScreen
import com.example.ui.screens.PlaylistsScreen
import com.example.ui.screens.SearchScreen
import com.example.ui.screens.SettingsScreen
import com.example.ui.theme.AetherCard
import com.example.ui.theme.CyanNeon
import com.example.ui.theme.StarlightHigh
import com.example.ui.theme.StarlightMuted

@Composable
fun MainApp(viewModel: MainViewModel) {
    val showOnboarding by viewModel.showOnboarding.collectAsState()
    val showLanding by viewModel.showLanding.collectAsState()
    val currentTab by viewModel.currentTab.collectAsState()
    val isPlayerExpanded by viewModel.isPlayerExpanded.collectAsState()
    val isQueueExpanded by viewModel.isQueueExpanded.collectAsState()
    val showEqualizer by viewModel.showEqualizer.collectAsState()
    val showSleepTimer by viewModel.showSleepTimer.collectAsState()
    val showPrivacy by viewModel.showPrivacyDisclosure.collectAsState()
    val songToAddToPlaylist by viewModel.songToAddToPlaylist.collectAsState()
    val selectedPlaylist by viewModel.selectedPlaylist.collectAsState()

    val playerState by viewModel.playerState.collectAsState()
    val queue by viewModel.queue.collectAsState()
    val currentIndex by viewModel.currentIndex.collectAsState()

    val featuredTracks by viewModel.featuredTracks.collectAsState()
    val recentlyPlayed by viewModel.recentlyPlayed.collectAsState()
    val favoriteSongs by viewModel.favoriteSongs.collectAsState()
    val localTracks by viewModel.localTracks.collectAsState()
    val playlists by viewModel.playlists.collectAsState()

    val searchQuery by viewModel.searchQuery.collectAsState()
    val searchResults by viewModel.searchResults.collectAsState()
    val isSearching by viewModel.isSearching.collectAsState()
    val searchError by viewModel.searchError.collectAsState()

    val librarySubTab by viewModel.librarySubTab.collectAsState()
    val isScanningLibrary by viewModel.isScanningLibrary.collectAsState()
    val isLoadingHome by viewModel.isLoadingHome.collectAsState()

    // Handle System Back Press
    BackHandler(enabled = isPlayerExpanded || isQueueExpanded || selectedPlaylist != null || currentTab != MainTab.HOME) {
        when {
            isQueueExpanded -> viewModel.setQueueExpanded(false)
            isPlayerExpanded -> viewModel.setPlayerExpanded(false)
            selectedPlaylist != null -> viewModel.selectPlaylist(null)
            currentTab != MainTab.HOME -> viewModel.setTab(MainTab.HOME)
        }
    }

    if (showOnboarding) {
        OnboardingScreen(onStartListening = { viewModel.completeOnboarding() })
        return
    }

    if (showLanding) {
        LandingScreen(onGetStarted = { viewModel.completeLanding() })
        return
    }

    CosmicBackground {
        Scaffold(
            containerColor = Color.Transparent,
            bottomBar = {
                Column(
                    modifier = Modifier
                        .navigationBarsPadding()
                        .background(Color.Transparent)
                ) {
                    // Mini Player
                    if (playerState.currentSong != null && !isPlayerExpanded) {
                        MiniPlayer(
                            playerState = playerState,
                            onExpandClick = { viewModel.setPlayerExpanded(true) },
                            onTogglePlayPause = { viewModel.togglePlayPause() },
                            onSkipNext = { viewModel.skipToNext() }
                        )
                    }

                    // Navigation Bar
                    NavigationBar(
                        containerColor = AetherCard.copy(alpha = 0.95f),
                        contentColor = StarlightHigh,
                        tonalElevation = 8.dp,
                        modifier = Modifier.testTag("bottom_nav_bar")
                    ) {
                        NavigationBarItem(
                            selected = currentTab == MainTab.HOME,
                            onClick = { viewModel.setTab(MainTab.HOME) },
                            icon = { Icon(Icons.Default.Home, contentDescription = "Home") },
                            label = { Text("Home", fontSize = 11.sp) },
                            colors = navBarColors(),
                            modifier = Modifier.testTag("nav_tab_home")
                        )
                        NavigationBarItem(
                            selected = currentTab == MainTab.SEARCH,
                            onClick = { viewModel.setTab(MainTab.SEARCH) },
                            icon = { Icon(Icons.Default.Search, contentDescription = "Search") },
                            label = { Text("Search", fontSize = 11.sp) },
                            colors = navBarColors(),
                            modifier = Modifier.testTag("nav_tab_search")
                        )
                        NavigationBarItem(
                            selected = currentTab == MainTab.LIBRARY,
                            onClick = { viewModel.setTab(MainTab.LIBRARY) },
                            icon = { Icon(Icons.Default.LibraryMusic, contentDescription = "Library") },
                            label = { Text("Library", fontSize = 11.sp) },
                            colors = navBarColors(),
                            modifier = Modifier.testTag("nav_tab_library")
                        )
                        NavigationBarItem(
                            selected = currentTab == MainTab.PLAYLISTS,
                            onClick = { viewModel.setTab(MainTab.PLAYLISTS) },
                            icon = { Icon(Icons.Default.QueueMusic, contentDescription = "Playlists") },
                            label = { Text("Playlists", fontSize = 11.sp) },
                            colors = navBarColors(),
                            modifier = Modifier.testTag("nav_tab_playlists")
                        )
                        NavigationBarItem(
                            selected = currentTab == MainTab.SETTINGS,
                            onClick = { viewModel.setTab(MainTab.SETTINGS) },
                            icon = { Icon(Icons.Default.Settings, contentDescription = "Settings") },
                            label = { Text("Settings", fontSize = 11.sp) },
                            colors = navBarColors(),
                            modifier = Modifier.testTag("nav_tab_settings")
                        )
                    }
                }
            }
        ) { paddingValues ->
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
            ) {
                when (currentTab) {
                    MainTab.HOME -> HomeScreen(
                        featuredTracks = featuredTracks,
                        recentlyPlayed = recentlyPlayed,
                        favoriteSongs = favoriteSongs,
                        playerState = playerState,
                        isLoading = isLoadingHome,
                        onSongClick = { song, list -> viewModel.playSong(song, list) },
                        onToggleFavorite = { viewModel.toggleFavorite(it) },
                        onPlayNext = { viewModel.playNext(it) },
                        onAddToPlaylist = { viewModel.setSongToAddToPlaylist(it) },
                        onDownload = { viewModel.downloadSong(it) },
                        onShuffleAll = {
                            val pool = featuredTracks + recentlyPlayed + localTracks
                            if (pool.isNotEmpty()) {
                                viewModel.playSong(pool.random(), pool)
                                viewModel.toggleShuffle()
                            }
                        },
                        onScanLocal = {
                            viewModel.setTab(MainTab.LIBRARY)
                            viewModel.scanLocalAudio()
                        },
                        onOpenPrivacy = { viewModel.setShowPrivacyDisclosure(true) },
                        onClearHistory = { viewModel.clearRecentlyPlayed() }
                    )

                    MainTab.SEARCH -> SearchScreen(
                        query = searchQuery,
                        onQueryChange = { viewModel.onSearchQueryChanged(it) },
                        searchResults = searchResults,
                        isSearching = isSearching,
                        errorMessage = searchError,
                        playerState = playerState,
                        onSongClick = { song, list -> viewModel.playSong(song, list) },
                        onToggleFavorite = { viewModel.toggleFavorite(it) },
                        onPlayNext = { viewModel.playNext(it) },
                        onAddToPlaylist = { viewModel.setSongToAddToPlaylist(it) },
                        onDownload = { viewModel.downloadSong(it) }
                    )

                    MainTab.LIBRARY -> LibraryScreen(
                        subTab = librarySubTab,
                        onSubTabSelect = { viewModel.setLibrarySubTab(it) },
                        localTracks = localTracks,
                        isScanning = isScanningLibrary,
                        playerState = playerState,
                        onScanClick = { viewModel.scanLocalAudio() },
                        onImportSafUri = { viewModel.importSafUri(it) },
                        onSongClick = { song, list -> viewModel.playSong(song, list) },
                        onToggleFavorite = { viewModel.toggleFavorite(it) },
                        onPlayNext = { viewModel.playNext(it) },
                        onAddToPlaylist = { viewModel.setSongToAddToPlaylist(it) },
                        onDownload = { viewModel.downloadSong(it) },
                        onRemoveDownload = { viewModel.removeDownload(it) }
                    )

                    MainTab.PLAYLISTS -> PlaylistsScreen(
                        playlists = playlists,
                        selectedPlaylist = selectedPlaylist,
                        allAvailableSongs = (featuredTracks + localTracks + recentlyPlayed).distinctBy { it.id },
                        playerState = playerState,
                        onSelectPlaylist = { viewModel.selectPlaylist(it) },
                        onCreatePlaylist = { name, desc -> viewModel.createPlaylist(name, desc) },
                        onDeletePlaylist = { viewModel.deletePlaylist(it) },
                        onRemoveSongFromPlaylist = { plId, sId -> viewModel.removeSongFromPlaylist(plId, sId) },
                        onSongClick = { song, list -> viewModel.playSong(song, list) },
                        onToggleFavorite = { viewModel.toggleFavorite(it) },
                        onPlayNext = { viewModel.playNext(it) },
                        onAddToPlaylist = { viewModel.setSongToAddToPlaylist(it) },
                        onDownload = { viewModel.downloadSong(it) }
                    )

                    MainTab.SETTINGS -> SettingsScreen(
                        onOpenPrivacyDisclosure = { viewModel.setShowPrivacyDisclosure(true) },
                        onClearHistory = { viewModel.clearRecentlyPlayed() }
                    )
                }

                // Full Player Sheet (slide in / out overlay)
                AnimatedVisibility(
                    visible = isPlayerExpanded && playerState.currentSong != null,
                    enter = slideInVertically(initialOffsetY = { it }),
                    exit = slideOutVertically(targetOffsetY = { it })
                ) {
                    FullPlayerSheet(
                        playerState = playerState,
                        onCollapse = { viewModel.setPlayerExpanded(false) },
                        onTogglePlayPause = { viewModel.togglePlayPause() },
                        onSkipNext = { viewModel.skipToNext() },
                        onSkipPrevious = { viewModel.skipToPrevious() },
                        onSeekTo = { viewModel.seekTo(it) },
                        onToggleRepeat = { viewModel.toggleRepeat() },
                        onToggleShuffle = { viewModel.toggleShuffle() },
                        onToggleFavorite = { viewModel.toggleFavorite(it) },
                        onOpenQueue = { viewModel.setQueueExpanded(true) },
                        onOpenEqualizer = { viewModel.setShowEqualizer(true) },
                        onOpenSleepTimer = { viewModel.setShowSleepTimer(true) },
                        onOpenAddToPlaylist = { viewModel.setSongToAddToPlaylist(it) },
                        onSetPlaybackSpeed = { viewModel.setPlaybackSpeed(it) }
                    )
                }

                // Queue Sheet
                AnimatedVisibility(
                    visible = isQueueExpanded,
                    enter = fadeIn(),
                    exit = fadeOut()
                ) {
                    QueueSheet(
                        queue = queue,
                        currentIndex = currentIndex,
                        onTrackSelect = { song, idx -> viewModel.playSong(song, queue) },
                        onRemoveFromQueue = { viewModel.removeFromQueue(it) },
                        onClearQueue = { viewModel.clearQueue() },
                        onDismiss = { viewModel.setQueueExpanded(false) }
                    )
                }
            }
        }
    }

    // Modal Dialogs
    if (showEqualizer) {
        EqualizerDialog(
            onPresetSelect = { viewModel.applyEqualizerPreset(it) },
            onBassStrengthChange = { viewModel.setBassStrength(it) },
            onDismiss = { viewModel.setShowEqualizer(false) }
        )
    }

    if (showSleepTimer) {
        SleepTimerDialog(
            activeRemainingSec = playerState.sleepTimerSeconds,
            onSetTimer = { viewModel.setSleepTimer(it) },
            onCancelTimer = { viewModel.cancelSleepTimer() },
            onDismiss = { viewModel.setShowSleepTimer(false) }
        )
    }

    if (showPrivacy) {
        PrivacyDisclosureDialog(
            onDismiss = { viewModel.setShowPrivacyDisclosure(false) }
        )
    }

    songToAddToPlaylist?.let { song ->
        AddToPlaylistDialog(
            playlists = playlists,
            song = song,
            onAddToPlaylist = { plId -> viewModel.addSongToPlaylist(plId, song) },
            onCreateNewPlaylist = {
                viewModel.createPlaylist("Favorites Mix", "Created from player")
            },
            onDismiss = { viewModel.setSongToAddToPlaylist(null) }
        )
    }
}

@Composable
private fun navBarColors() = NavigationBarItemDefaults.colors(
    selectedIconColor = CyanNeon,
    selectedTextColor = CyanNeon,
    unselectedIconColor = StarlightMuted,
    unselectedTextColor = StarlightMuted,
    indicatorColor = Color(0xFF1E2845)
)