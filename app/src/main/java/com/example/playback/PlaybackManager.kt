package com.example.playback

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import androidx.annotation.OptIn
import androidx.media3.common.AudioAttributes
import androidx.media3.common.C
import androidx.media3.common.MediaItem
import androidx.media3.common.MediaMetadata
import androidx.media3.common.PlaybackException
import androidx.media3.common.PlaybackParameters
import androidx.media3.common.Player
import androidx.media3.common.util.UnstableApi
import androidx.media3.exoplayer.ExoPlayer
import com.example.data.model.AudioSource
import com.example.data.model.RepeatMode
import com.example.data.model.Song
import com.example.data.repository.MusicRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch

data class PlayerState(
    val currentSong: Song? = null,
    val isPlaying: Boolean = false,
    val isBuffering: Boolean = false,
    val currentPositionMs: Long = 0L,
    val durationMs: Long = 0L,
    val repeatMode: RepeatMode = RepeatMode.OFF,
    val isShuffleEnabled: Boolean = false,
    val playbackSpeed: Float = 1.0f,
    val sleepTimerSeconds: Long? = null,
    val errorMessage: String? = null
)

@OptIn(UnstableApi::class)
class PlaybackManager private constructor(private val context: Context) {

    private val scope = CoroutineScope(Dispatchers.Main + Job())
    private var positionJob: Job? = null
    private var sleepTimerJob: Job? = null

    val audioFxManager = AudioFxManager()
    lateinit var musicRepository: MusicRepository

    val exoPlayer: ExoPlayer by lazy {
        ExoPlayer.Builder(context)
            .setAudioAttributes(
                AudioAttributes.Builder()
                    .setContentType(C.AUDIO_CONTENT_TYPE_MUSIC)
                    .setUsage(C.USAGE_MEDIA)
                    .build(),
                true
            )
            .setHandleAudioBecomingNoisy(true)
            .setWakeMode(C.WAKE_MODE_NETWORK)
            .build().apply {
                addListener(playerListener)
            }
    }

    private val _queue = MutableStateFlow<List<Song>>(emptyList())
    val queue: StateFlow<List<Song>> = _queue.asStateFlow()

    private val _currentIndex = MutableStateFlow(-1)
    val currentIndex: StateFlow<Int> = _currentIndex.asStateFlow()

    private val _playerState = MutableStateFlow(PlayerState())
    val playerState: StateFlow<PlayerState> = _playerState.asStateFlow()

    private var originalQueue: List<Song> = emptyList()

    private val playerListener = object : Player.Listener {
        override fun onIsPlayingChanged(isPlaying: Boolean) {
            _playerState.value = _playerState.value.copy(isPlaying = isPlaying)
            if (isPlaying) {
                startPositionTracker()
            } else {
                stopPositionTracker()
            }
        }

        override fun onPlaybackStateChanged(playbackState: Int) {
            when (playbackState) {
                Player.STATE_BUFFERING -> {
                    _playerState.value = _playerState.value.copy(isBuffering = true, errorMessage = null)
                }
                Player.STATE_READY -> {
                    _playerState.value = _playerState.value.copy(
                        isBuffering = false,
                        durationMs = exoPlayer.duration.coerceAtLeast(0L),
                        errorMessage = null
                    )
                    audioFxManager.attachAudioSession(exoPlayer.audioSessionId)
                }
                Player.STATE_ENDED -> {
                    _playerState.value = _playerState.value.copy(isBuffering = false)
                    handleTrackEnded()
                }
                Player.STATE_IDLE -> {
                    _playerState.value = _playerState.value.copy(isBuffering = false)
                }
            }
        }

        override fun onPlayerError(error: PlaybackException) {
            val message = when (error.errorCode) {
                PlaybackException.ERROR_CODE_IO_NETWORK_CONNECTION_FAILED,
                PlaybackException.ERROR_CODE_IO_NETWORK_CONNECTION_TIMEOUT ->
                    "Network error. Check connection or try another source."
                PlaybackException.ERROR_CODE_PARSING_CONTAINER_UNSUPPORTED,
                PlaybackException.ERROR_CODE_DECODER_INIT_FAILED ->
                    "Audio format unsupported or stream unavailable."
                else -> error.localizedMessage ?: "Playback error encountered."
            }
            _playerState.value = _playerState.value.copy(
                isPlaying = false,
                isBuffering = false,
                errorMessage = message
            )
        }
    }

    fun initRepository(repo: MusicRepository) {
        this.musicRepository = repo
    }

    private fun startPlaybackService() {
        try {
            val intent = Intent(context, MediaPlaybackService::class.java)
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                context.startForegroundService(intent)
            } else {
                context.startService(intent)
            }
        } catch (e: Exception) {
            // Ignore if already active or background restrictions
        }
    }

    fun playSong(song: Song, newQueue: List<Song> = listOf(song)) {
        scope.launch {
            _playerState.value = _playerState.value.copy(isBuffering = true, errorMessage = null)

            // Start foreground media session service for lock-screen and notification controls
            startPlaybackService()

            // Resolve streaming URL asynchronously on Dispatchers.IO (Piped API / Invidious with M4A/AAC priority)
            val playableUrl = kotlinx.coroutines.withContext(Dispatchers.IO) {
                try {
                    if (::musicRepository.isInitialized) {
                        musicRepository.resolveStreamUrl(song)
                    } else {
                        song.streamUrl
                    }
                } catch (e: Exception) {
                    song.streamUrl
                }
            }

            val finalSong = song.copy(streamUrl = playableUrl)
            originalQueue = newQueue
            _queue.value = newQueue

            val index = newQueue.indexOfFirst { it.id == song.id }.let { if (it == -1) 0 else it }
            _currentIndex.value = index

            _playerState.value = _playerState.value.copy(
                currentSong = finalSong,
                currentPositionMs = 0L,
                durationMs = finalSong.durationMs
            )

            // Prepare MediaItem
            val mediaMetadata = MediaMetadata.Builder()
                .setTitle(finalSong.title)
                .setArtist(finalSong.artist)
                .setAlbumTitle(finalSong.album)
                .setArtworkUri(finalSong.artworkUrl?.let { Uri.parse(it) })
                .build()

            val mediaItem = MediaItem.Builder()
                .setUri(Uri.parse(playableUrl))
                .setMediaMetadata(mediaMetadata)
                .build()

            exoPlayer.setMediaItem(mediaItem)
            exoPlayer.prepare()
            exoPlayer.play()

            if (::musicRepository.isInitialized) {
                musicRepository.addToRecentlyPlayed(finalSong)
            }
        }
    }

    fun togglePlayPause() {
        if (exoPlayer.isPlaying) {
            exoPlayer.pause()
        } else {
            if (exoPlayer.playbackState == Player.STATE_IDLE || exoPlayer.mediaItemCount == 0) {
                _playerState.value.currentSong?.let { playSong(it, _queue.value) }
            } else {
                exoPlayer.play()
            }
        }
    }

    fun pause() {
        exoPlayer.pause()
    }

    fun resume() {
        exoPlayer.play()
    }

    fun seekTo(positionMs: Long) {
        val target = positionMs.coerceIn(0L, _playerState.value.durationMs.coerceAtLeast(0L))
        exoPlayer.seekTo(target)
        _playerState.value = _playerState.value.copy(currentPositionMs = target)
    }

    fun skipToNext() {
        val q = _queue.value
        if (q.isEmpty()) return
        val nextIdx = if (_currentIndex.value + 1 < q.size) {
            _currentIndex.value + 1
        } else {
            if (_playerState.value.repeatMode == RepeatMode.ALL) 0 else return
        }
        playSong(q[nextIdx], q)
    }

    fun skipToPrevious() {
        val q = _queue.value
        if (q.isEmpty()) return
        if (exoPlayer.currentPosition > 3000L) {
            seekTo(0L)
            return
        }
        val prevIdx = if (_currentIndex.value - 1 >= 0) {
            _currentIndex.value - 1
        } else {
            if (_playerState.value.repeatMode == RepeatMode.ALL) q.size - 1 else 0
        }
        playSong(q[prevIdx], q)
    }

    private fun handleTrackEnded() {
        when (_playerState.value.repeatMode) {
            RepeatMode.ONE -> {
                exoPlayer.seekTo(0L)
                exoPlayer.play()
            }
            RepeatMode.ALL -> {
                skipToNext()
            }
            RepeatMode.OFF -> {
                val q = _queue.value
                if (_currentIndex.value + 1 < q.size) {
                    skipToNext()
                } else {
                    _playerState.value = _playerState.value.copy(isPlaying = false)
                }
            }
        }
    }

    fun toggleRepeatMode() {
        val next = when (_playerState.value.repeatMode) {
            RepeatMode.OFF -> RepeatMode.ALL
            RepeatMode.ALL -> RepeatMode.ONE
            RepeatMode.ONE -> RepeatMode.OFF
        }
        _playerState.value = _playerState.value.copy(repeatMode = next)
    }

    fun toggleShuffle() {
        val newShuffle = !_playerState.value.isShuffleEnabled
        _playerState.value = _playerState.value.copy(isShuffleEnabled = newShuffle)

        val currentSong = _playerState.value.currentSong
        if (newShuffle) {
            val shuffled = originalQueue.toMutableList()
            if (currentSong != null) {
                shuffled.remove(currentSong)
                shuffled.shuffle()
                shuffled.add(0, currentSong)
            } else {
                shuffled.shuffle()
            }
            _queue.value = shuffled
            _currentIndex.value = 0
        } else {
            _queue.value = originalQueue
            _currentIndex.value = originalQueue.indexOfFirst { it.id == currentSong?.id }.coerceAtLeast(0)
        }
    }

    fun setPlaybackSpeed(speed: Float) {
        val safeSpeed = speed.coerceIn(0.5f, 2.5f)
        exoPlayer.playbackParameters = PlaybackParameters(safeSpeed)
        _playerState.value = _playerState.value.copy(playbackSpeed = safeSpeed)
    }

    fun addToQueue(song: Song) {
        val updated = _queue.value + song
        _queue.value = updated
        originalQueue = originalQueue + song
    }

    fun playNext(song: Song) {
        val currentIdx = _currentIndex.value
        val list = _queue.value.toMutableList()
        val insertIdx = (currentIdx + 1).coerceAtMost(list.size)
        list.add(insertIdx, song)
        _queue.value = list
    }

    fun removeFromQueue(index: Int) {
        if (index !in _queue.value.indices) return
        val list = _queue.value.toMutableList()
        list.removeAt(index)
        _queue.value = list
        if (index < _currentIndex.value) {
            _currentIndex.value -= 1
        }
    }

    fun clearQueue() {
        val currentSong = _playerState.value.currentSong
        if (currentSong != null) {
            _queue.value = listOf(currentSong)
            _currentIndex.value = 0
        } else {
            _queue.value = emptyList()
            _currentIndex.value = -1
        }
    }

    fun setSleepTimer(minutes: Int) {
        cancelSleepTimer()
        val totalSec = minutes * 60L
        _playerState.value = _playerState.value.copy(sleepTimerSeconds = totalSec)

        sleepTimerJob = scope.launch {
            var remaining = totalSec
            while (isActive && remaining > 0) {
                delay(1000L)
                remaining--
                _playerState.value = _playerState.value.copy(sleepTimerSeconds = remaining)
            }
            if (remaining <= 0) {
                pause()
                _playerState.value = _playerState.value.copy(sleepTimerSeconds = null)
            }
        }
    }

    fun cancelSleepTimer() {
        sleepTimerJob?.cancel()
        sleepTimerJob = null
        _playerState.value = _playerState.value.copy(sleepTimerSeconds = null)
    }

    private fun startPositionTracker() {
        positionJob?.cancel()
        positionJob = scope.launch {
            while (isActive) {
                if (exoPlayer.isPlaying) {
                    val pos = exoPlayer.currentPosition
                    val dur = exoPlayer.duration.coerceAtLeast(0L)
                    _playerState.value = _playerState.value.copy(
                        currentPositionMs = pos,
                        durationMs = if (dur > 0L) dur else _playerState.value.durationMs
                    )
                }
                delay(250L)
            }
        }
    }

    private fun stopPositionTracker() {
        positionJob?.cancel()
        positionJob = null
    }

    fun release() {
        stopPositionTracker()
        cancelSleepTimer()
        audioFxManager.release()
        exoPlayer.removeListener(playerListener)
        exoPlayer.release()
    }

    companion object {
        @Volatile
        private var instance: PlaybackManager? = null

        fun getInstance(context: Context): PlaybackManager {
            return instance ?: synchronized(this) {
                instance ?: PlaybackManager(context.applicationContext).also { instance = it }
            }
        }
    }
}
