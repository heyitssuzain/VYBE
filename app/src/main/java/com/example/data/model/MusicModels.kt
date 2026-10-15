package com.example.data.model

enum class AudioSource(
    val displayName: String,
    val description: String,
    val isLegalPublic: Boolean
) {
    LOCAL("Local", "On-device audio files", true),
    YOUTUBE("YouTube Music", "YouTube via Piped & yt-dlp extractors", true)
}

enum class RepeatMode {
    OFF, ALL, ONE
}

data class Song(
    val id: String,
    val title: String,
    val artist: String,
    val album: String = "",
    val artworkUrl: String? = null,
    val streamUrl: String,
    val durationMs: Long = 0L,
    val source: AudioSource = AudioSource.LOCAL,
    val license: String? = null,
    val isFavorite: Boolean = false,
    val isDownloaded: Boolean = false,
    val genre: String? = null,
    val year: String? = null,
    val lyrics: String? = null
)

data class Playlist(
    val id: String,
    val name: String,
    val description: String = "",
    val createdAt: Long = System.currentTimeMillis(),
    val songIds: List<String> = emptyList(),
    val coverUrl: String? = null
)
