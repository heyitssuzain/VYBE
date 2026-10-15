package com.example.ui

import android.app.Application
import android.content.Context
import android.net.Uri
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.model.AudioSource
import com.example.data.model.Playlist
import com.example.data.model.Song
import com.example.data.repository.MusicRepository
import com.example.playback.EqualizerPreset
import com.example.playback.PlaybackManager
import com.example.playback.PlayerState
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

enum class MainTab {
    HOME, SEARCH, LIBRARY, PLAYLISTS, SETTINGS
}

enum class LibrarySubTab {
    TRACKS, ARTISTS, ALBUMS, FOLDERS
}

class MainViewModel(application: Application) : AndroidViewModel(application) {

    val repository = MusicRepository(application)
    val playbackManager = PlaybackManager.getInstance(application).apply {
        initRepository(repository)
    }

    private val prefs = application.getSharedPreferences("vybe_ui_prefs", Context.MODE_PRIVATE)

    // Navigation and Sheets
    private val _currentTab = MutableStateFlow(MainTab.HOME)
    val currentTab: StateFlow<MainTab> = _currentTab.asStateFlow()

    private val _isPlayerExpanded = MutableStateFlow(false)
    val isPlayerExpanded: StateFlow<Boolean> = _isPlayerExpanded.asStateFlow()

    private val _isQueueExpanded = MutableStateFlow(false)
    val isQueueExpanded: StateFlow<Boolean> = _isQueueExpanded.asStateFlow()

    private val _showEqualizer = MutableStateFlow(false)
    val showEqualizer: StateFlow<Boolean> = _showEqualizer.asStateFlow()

    private val _showSleepTimer = MutableStateFlow(false)
    val showSleepTimer: StateFlow<Boolean> = _showSleepTimer.asStateFlow()

    private val _showPrivacyDisclosure = MutableStateFlow(false)
    val showPrivacyDisclosure: StateFlow<Boolean> = _showPrivacyDisclosure.asStateFlow()

    private val _showOnboarding = MutableStateFlow(!prefs.getBoolean("onboarding_complete", false))
    val showOnboarding: StateFlow<Boolean> = _showOnboarding.asStateFlow()

    private val _showLanding = MutableStateFlow(!prefs.getBoolean("landing_complete", false))
    val showLanding: StateFlow<Boolean> = _showLanding.asStateFlow()

    private val _songToAddToPlaylist = MutableStateFlow<Song?>(null)
    val songToAddToPlaylist: StateFlow<Song?> = _songToAddToPlaylist.asStateFlow()

    private val _selectedPlaylist = MutableStateFlow<Playlist?>(null)
    val selectedPlaylist: StateFlow<Playlist?> = _selectedPlaylist.asStateFlow()

    // Home Content
    private val _featuredTracks = MutableStateFlow<List<Song>>(emptyList())
    val featuredTracks: StateFlow<List<Song>> = _featuredTracks.asStateFlow()

    private val _isLoadingHome = MutableStateFlow(false)
    val isLoadingHome: StateFlow<Boolean> = _isLoadingHome.asStateFlow()

    // Search Content
    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    private val _searchResults = MutableStateFlow<List<Song>>(emptyList())
    val searchResults: StateFlow<List<Song>> = _searchResults.asStateFlow()

    private val _isSearching = MutableStateFlow(false)
    val isSearching: StateFlow<Boolean> = _isSearching.asStateFlow()

    private val _searchError = MutableStateFlow<String?>(null)
    val searchError: StateFlow<String?> = _searchError.asStateFlow()

    private var searchJob: Job? = null

    // Library Content
    private val _librarySubTab = MutableStateFlow(LibrarySubTab.TRACKS)
    val librarySubTab: StateFlow<LibrarySubTab> = _librarySubTab.asStateFlow()

    private val _localTracks = MutableStateFlow<List<Song>>(emptyList())
    val localTracks: StateFlow<List<Song>> = _localTracks.asStateFlow()

    private val _isScanningLibrary = MutableStateFlow(false)
    val isScanningLibrary: StateFlow<Boolean> = _isScanningLibrary.asStateFlow()

    // Playback state exposed to UI
    val playerState: StateFlow<PlayerState> = playbackManager.playerState
    val queue: StateFlow<List<Song>> = playbackManager.queue
    val currentIndex: StateFlow<Int> = playbackManager.currentIndex

    // Persistence bindings
    val favorites: StateFlow<Set<String>> = repository.favorites
    val recentlyPlayed: StateFlow<List<Song>> = repository.recentlyPlayed
    val playlists: StateFlow<List<Playlist>> = repository.playlists
    val downloadedSongs: StateFlow<Set<String>> = repository.downloadedSongs

    val favoriteSongs: StateFlow<List<Song>> = combine(
        featuredTracks,
        localTracks,
        recentlyPlayed,
        favorites
    ) { feat, local, recent, favIds ->
        val pool = (feat + local + recent).distinctBy { it.id }
        pool.filter { favIds.contains(it.id) }.map { it.copy(isFavorite = true) }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    init {
        loadHomeData()
        scanLocalAudio()
    }

    fun completeOnboarding() {
        prefs.edit().putBoolean("onboarding_complete", true).apply()
        _showOnboarding.value = false
    }

    fun completeLanding() {
        prefs.edit().putBoolean("landing_complete", true).apply()
        _showLanding.value = false
    }

    fun setTab(tab: MainTab) {
        _currentTab.value = tab
    }

    fun setPlayerExpanded(expanded: Boolean) {
        _isPlayerExpanded.value = expanded
    }

    fun setQueueExpanded(expanded: Boolean) {
        _isQueueExpanded.value = expanded
    }

    fun setShowEqualizer(show: Boolean) {
        _showEqualizer.value = show
    }

    fun setShowSleepTimer(show: Boolean) {
        _showSleepTimer.value = show
    }

    fun setShowPrivacyDisclosure(show: Boolean) {
        _showPrivacyDisclosure.value = show
    }

    fun setSongToAddToPlaylist(song: Song?) {
        _songToAddToPlaylist.value = song
    }

    fun selectPlaylist(playlist: Playlist?) {
        _selectedPlaylist.value = playlist
    }

    fun setLibrarySubTab(subTab: LibrarySubTab) {
        _librarySubTab.value = subTab
    }

    fun loadHomeData() {
        viewModelScope.launch {
            _isLoadingHome.value = true
            try {
                val showcase = repository.getCuratedShowcase()
                _featuredTracks.value = showcase
            } catch (e: Exception) {
                _featuredTracks.value = emptyList()
            } finally {
                _isLoadingHome.value = false
            }
        }
    }

    fun scanLocalAudio() {
        viewModelScope.launch {
            _isScanningLibrary.value = true
            try {
                val tracks = repository.scanLocalTracks()
                _localTracks.value = tracks
            } catch (e: Exception) {
                _localTracks.value = emptyList()
            } finally {
                _isScanningLibrary.value = false
            }
        }
    }

    fun importSafUri(uri: Uri) {
        viewModelScope.launch {
            val song = repository.importSafTrack(uri)
            val updated = listOf(song) + _localTracks.value.filter { it.id != song.id }
            _localTracks.value = updated
            playbackManager.playSong(song, updated)
        }
    }

    fun onSearchQueryChanged(newQuery: String) {
        _searchQuery.value = newQuery
        searchJob?.cancel()
        if (newQuery.isBlank()) {
            _searchResults.value = emptyList()
            _searchError.value = null
            _isSearching.value = false
            return
        }

        searchJob = viewModelScope.launch {
            delay(300L) // Faster debounce for instant feel
            executeSearch(newQuery)
        }
    }

    private suspend fun executeSearch(query: String) {
        _isSearching.value = true
        _searchError.value = null
        try {
            val results = repository.searchSongs(query, null)
            _searchResults.value = results
            if (results.isEmpty()) {
                _searchError.value = "No results found for \"$query\". Try a different search."
            }
        } catch (e: Exception) {
            _searchError.value = "Network error. Please check connection and retry."
            _searchResults.value = emptyList()
        } finally {
            _isSearching.value = false
        }
    }

    // Playback commands
    fun playSong(song: Song, queue: List<Song> = listOf(song)) {
        playbackManager.playSong(song, queue)
    }

    fun togglePlayPause() = playbackManager.togglePlayPause()
    fun skipToNext() = playbackManager.skipToNext()
    fun skipToPrevious() = playbackManager.skipToPrevious()
    fun seekTo(positionMs: Long) = playbackManager.seekTo(positionMs)
    fun toggleRepeat() = playbackManager.toggleRepeatMode()
    fun toggleShuffle() = playbackManager.toggleShuffle()
    fun setPlaybackSpeed(speed: Float) = playbackManager.setPlaybackSpeed(speed)
    fun setSleepTimer(minutes: Int) = playbackManager.setSleepTimer(minutes)
    fun cancelSleepTimer() = playbackManager.cancelSleepTimer()

    fun playNext(song: Song) = playbackManager.playNext(song)
    fun addToQueue(song: Song) = playbackManager.addToQueue(song)
    fun removeFromQueue(index: Int) = playbackManager.removeFromQueue(index)
    fun clearQueue() = playbackManager.clearQueue()

    fun toggleFavorite(song: Song) {
        repository.toggleFavorite(song)
        // Refresh local views
        _featuredTracks.value = _featuredTracks.value.map {
            if (it.id == song.id) it.copy(isFavorite = !it.isFavorite) else it
        }
        _searchResults.value = _searchResults.value.map {
            if (it.id == song.id) it.copy(isFavorite = !it.isFavorite) else it
        }
        _localTracks.value = _localTracks.value.map {
            if (it.id == song.id) it.copy(isFavorite = !it.isFavorite) else it
        }
    }

    fun clearRecentlyPlayed() = repository.clearRecentlyPlayed()

    fun createPlaylist(name: String, description: String = "") {
        repository.createPlaylist(name, description)
    }

    fun renamePlaylist(id: String, newName: String) {
        repository.renamePlaylist(id, newName)
        _selectedPlaylist.value = _selectedPlaylist.value?.let {
            if (it.id == id) it.copy(name = newName) else it
        }
    }

    fun deletePlaylist(id: String) {
        repository.deletePlaylist(id)
        if (_selectedPlaylist.value?.id == id) {
            _selectedPlaylist.value = null
        }
    }

    fun addSongToPlaylist(playlistId: String, song: Song) {
        repository.addSongToPlaylist(playlistId, song.id)
        _songToAddToPlaylist.value = null
    }

    fun removeSongFromPlaylist(playlistId: String, songId: String) {
        repository.removeSongFromPlaylist(playlistId, songId)
        _selectedPlaylist.value = _selectedPlaylist.value?.let { pl ->
            if (pl.id == playlistId) pl.copy(songIds = pl.songIds - songId) else pl
        }
    }

    fun applyEqualizerPreset(preset: EqualizerPreset) {
        playbackManager.audioFxManager.applyPreset(preset)
    }

    fun setBassStrength(strength: Int) {
        playbackManager.audioFxManager.setBassStrength(strength)
    }

    // Download functionality
    fun downloadSong(song: Song) {
        viewModelScope.launch {
            // Simulate download - in real app, this would download the file
            // For now, we'll mark it as downloaded immediately
            repository.markAsDownloaded(song.id)
            
            // Update local tracks to show downloaded state
            _localTracks.value = _localTracks.value.map {
                if (it.id == song.id) it.copy(isDownloaded = true) else it
            }
            _searchResults.value = _searchResults.value.map {
                if (it.id == song.id) it.copy(isDownloaded = true) else it
            }
            _featuredTracks.value = _featuredTracks.value.map {
                if (it.id == song.id) it.copy(isDownloaded = true) else it
            }
        }
    }

    fun removeDownload(song: Song) {
        repository.markAsRemoved(song.id)
        _localTracks.value = _localTracks.value.map {
            if (it.id == song.id) it.copy(isDownloaded = false) else it
        }
        _searchResults.value = _searchResults.value.map {
            if (it.id == song.id) it.copy(isDownloaded = false) else it
        }
        _featuredTracks.value = _featuredTracks.value.map {
            if (it.id == song.id) it.copy(isDownloaded = false) else it
        }
    }

    override fun onCleared() {
        super.onCleared()
        // Do not release playbackManager here to allow background playback to persist
    }
}