package com.example.data.repository

import android.content.Context
import android.content.SharedPreferences
import android.net.Uri
import com.example.data.model.AudioSource
import com.example.data.model.Playlist
import com.example.data.model.Song
import com.example.data.provider.AudioSourceProvider
import com.example.data.provider.LocalAudioSourceProvider
import com.example.data.provider.YouTubeSourceProvider
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.json.JSONArray
import org.json.JSONObject
import java.util.UUID

class MusicRepository(private val context: Context) {

    private val prefs: SharedPreferences =
        context.getSharedPreferences("vybe_music_prefs", Context.MODE_PRIVATE)

    val localProvider = LocalAudioSourceProvider(context)
    val youtubeProvider = YouTubeSourceProvider()

    private val providers: Map<AudioSource, AudioSourceProvider> = mapOf(
        AudioSource.LOCAL to localProvider,
        AudioSource.YOUTUBE to youtubeProvider
    )

    private val _favorites = MutableStateFlow<Set<String>>(emptySet())
    val favorites: StateFlow<Set<String>> = _favorites.asStateFlow()

    private val _recentlyPlayed = MutableStateFlow<List<Song>>(emptyList())
    val recentlyPlayed: StateFlow<List<Song>> = _recentlyPlayed.asStateFlow()

    private val _playlists = MutableStateFlow<List<Playlist>>(emptyList())
    val playlists: StateFlow<List<Playlist>> = _playlists.asStateFlow()

    private val _downloadedSongs = MutableStateFlow<Set<String>>(emptySet())
    val downloadedSongs: StateFlow<Set<String>> = _downloadedSongs.asStateFlow()

    init {
        loadPersistedState()
    }

    private fun loadPersistedState() {
        val favs = prefs.getStringSet("favorite_ids", emptySet()) ?: emptySet()
        _favorites.value = favs

        // Load recently played
        val recentJson = prefs.getString("recently_played_json", "[]") ?: "[]"
        _recentlyPlayed.value = parseSongsJson(recentJson)

        // Load playlists
        val playlistsJson = prefs.getString("playlists_json", "[]") ?: "[]"
        _playlists.value = parsePlaylistsJson(playlistsJson)
        if (_playlists.value.isEmpty()) {
            createDefaultPlaylists()
        }

        // Load downloaded songs
        val downloadedJson = prefs.getString("downloaded_songs_json", "[]") ?: "[]"
        _downloadedSongs.value = parseDownloadedJson(downloadedJson).toSet()
    }

    private fun createDefaultPlaylists() {
        val favorites = Playlist(
            id = "pl_favorites",
            name = "Favorites Mix",
            description = "Your liked tracks",
            songIds = listOf()
        )
        _playlists.value = listOf(favorites)
        savePlaylists()
    }

    suspend fun getCuratedShowcase(): List<Song> = withContext(Dispatchers.IO) {
        val list = youtubeProvider.getFeatured()
        list.map { it.copy(isFavorite = _favorites.value.contains(it.id)) }
    }

    suspend fun scanLocalTracks(): List<Song> = withContext(Dispatchers.IO) {
        val songs = localProvider.scanLocalLibrary()
        songs.map { it.copy(isFavorite = _favorites.value.contains(it.id), isDownloaded = _downloadedSongs.value.contains(it.id)) }
    }

    suspend fun importSafTrack(uri: Uri): Song = withContext(Dispatchers.IO) {
        val song = localProvider.parseFromSafUri(uri)
        addToRecentlyPlayed(song)
        song.copy(isFavorite = _favorites.value.contains(song.id), isDownloaded = _downloadedSongs.value.contains(song.id))
    }

    suspend fun searchSongs(query: String, filter: AudioSource? = null): List<Song> = withContext(Dispatchers.IO) {
        val list = mutableListOf<Song>()
        val cleanQuery = query.trim()
        if (cleanQuery.isBlank()) return@withContext emptyList()

        if (filter != null) {
            val p = providers[filter]
            if (p != null) {
                try { list.addAll(p.search(cleanQuery)) } catch (e: Exception) {}
            }
        } else {
            // Priority: YouTube Search with real official metadata & high-res artwork
            try { list.addAll(youtubeProvider.search(cleanQuery, 20)) } catch (e: Exception) {}
            // Local on-device files
            try { list.addAll(localProvider.search(cleanQuery, 10)) } catch (e: Exception) {}
        }

        list.distinctBy { it.id }.map { it.copy(isFavorite = _favorites.value.contains(it.id), isDownloaded = _downloadedSongs.value.contains(it.id)) }
    }

    suspend fun resolveStreamUrl(song: Song): String {
        val provider = providers[song.source]
        return provider?.resolveStreamUrl(song) ?: song.streamUrl
    }

    fun toggleFavorite(song: Song) {
        val current = _favorites.value.toMutableSet()
        if (current.contains(song.id)) {
            current.remove(song.id)
        } else {
            current.add(song.id)
        }
        _favorites.value = current
        prefs.edit().putStringSet("favorite_ids", current).apply()
    }

    fun isFavorite(songId: String): Boolean {
        return _favorites.value.contains(songId)
    }

    fun addToRecentlyPlayed(song: Song) {
        val list = _recentlyPlayed.value.toMutableList()
        list.removeAll { it.id == song.id }
        list.add(0, song)
        val capped = list.take(50)
        _recentlyPlayed.value = capped
        prefs.edit().putString("recently_played_json", serializeSongsJson(capped)).apply()
    }

    fun clearRecentlyPlayed() {
        _recentlyPlayed.value = emptyList()
        prefs.edit().remove("recently_played_json").apply()
    }

    fun createPlaylist(name: String, description: String = ""): Playlist {
        val playlist = Playlist(
            id = UUID.randomUUID().toString(),
            name = name.ifBlank { "Untitled Playlist" },
            description = description
        )
        val current = _playlists.value.toMutableList()
        current.add(playlist)
        _playlists.value = current
        savePlaylists()
        return playlist
    }

    fun renamePlaylist(id: String, newName: String) {
        _playlists.value = _playlists.value.map {
            if (it.id == id) it.copy(name = newName) else it
        }
        savePlaylists()
    }

    fun deletePlaylist(id: String) {
        _playlists.value = _playlists.value.filter { it.id != id }
        savePlaylists()
    }

    fun addSongToPlaylist(playlistId: String, songId: String) {
        _playlists.value = _playlists.value.map { pl ->
            if (pl.id == playlistId && !pl.songIds.contains(songId)) {
                pl.copy(songIds = pl.songIds + songId)
            } else pl
        }
        savePlaylists()
    }

    fun removeSongFromPlaylist(playlistId: String, songId: String) {
        _playlists.value = _playlists.value.map { pl ->
            if (pl.id == playlistId) {
                pl.copy(songIds = pl.songIds - songId)
            } else pl
        }
        savePlaylists()
    }

    // Download management
    fun markAsDownloaded(songId: String) {
        val current = _downloadedSongs.value.toMutableSet()
        current.add(songId)
        _downloadedSongs.value = current
        prefs.edit().putString("downloaded_songs_json", serializeDownloadedJson(current)).apply()
    }

    fun markAsRemoved(songId: String) {
        val current = _downloadedSongs.value.toMutableSet()
        current.remove(songId)
        _downloadedSongs.value = current
        prefs.edit().putString("downloaded_songs_json", serializeDownloadedJson(current)).apply()
    }

    fun isDownloaded(songId: String): Boolean {
        return _downloadedSongs.value.contains(songId)
    }

    private fun savePlaylists() {
        val array = JSONArray()
        for (pl in _playlists.value) {
            val obj = JSONObject()
            obj.put("id", pl.id)
            obj.put("name", pl.name)
            obj.put("description", pl.description)
            obj.put("createdAt", pl.createdAt)
            val ids = JSONArray()
            for (sid in pl.songIds) ids.put(sid)
            obj.put("songIds", ids)
            array.put(obj)
        }
        prefs.edit().putString("playlists_json", array.toString()).apply()
    }

    private fun parsePlaylistsJson(jsonStr: String): List<Playlist> {
        val list = mutableListOf<Playlist>()
        try {
            val array = JSONArray(jsonStr)
            for (i in 0 until array.length()) {
                val obj = array.optJSONObject(i) ?: continue
                val id = obj.optString("id")
                val name = obj.optString("name")
                val desc = obj.optString("description")
                val created = obj.optLong("createdAt")
                val idsArr = obj.optJSONArray("songIds")
                val songIds = mutableListOf<String>()
                if (idsArr != null) {
                    for (j in 0 until idsArr.length()) {
                        songIds.add(idsArr.optString(j))
                    }
                }
                list.add(Playlist(id, name, desc, created, songIds))
            }
        } catch (e: Exception) {}
        return list
    }

    private fun serializeSongsJson(songs: List<Song>): String {
        val array = JSONArray()
        for (s in songs) {
            val obj = JSONObject()
            obj.put("id", s.id)
            obj.put("title", s.title)
            obj.put("artist", s.artist)
            obj.put("album", s.album)
            obj.put("artworkUrl", s.artworkUrl ?: "")
            obj.put("streamUrl", s.streamUrl)
            obj.put("durationMs", s.durationMs)
            obj.put("source", s.source.name)
            obj.put("license", s.license ?: "")
            array.put(obj)
        }
        return array.toString()
    }

    private fun parseSongsJson(jsonStr: String): List<Song> {
        val list = mutableListOf<Song>()
        try {
            val array = JSONArray(jsonStr)
            for (i in 0 until array.length()) {
                val obj = array.optJSONObject(i) ?: continue
                val id = obj.optString("id")
                val title = obj.optString("title")
                val artist = obj.optString("artist")
                val album = obj.optString("album")
                val artwork = obj.optString("artworkUrl").ifBlank { null }
                val stream = obj.optString("streamUrl")
                val duration = obj.optLong("durationMs")
                val srcName = obj.optString("source", AudioSource.LOCAL.name)
                val src = try { AudioSource.valueOf(srcName) } catch (e: Exception) { AudioSource.LOCAL }
                val license = obj.optString("license")

                list.add(
                    Song(
                        id = id,
                        title = title,
                        artist = artist,
                        album = album,
                        artworkUrl = artwork,
                        streamUrl = stream,
                        durationMs = duration,
                        source = src,
                        license = license,
                        isFavorite = _favorites.value.contains(id)
                    )
                )
            }
        } catch (e: Exception) {}
        return list
    }

    private fun parseDownloadedJson(jsonStr: String): List<String> {
        val list = mutableListOf<String>()
        try {
            val array = JSONArray(jsonStr)
            for (i in 0 until array.length()) {
                list.add(array.optString(i))
            }
        } catch (e: Exception) {}
        return list
    }

    private fun serializeDownloadedJson(ids: Set<String>): String {
        val array = JSONArray()
        for (id in ids) array.put(id)
        return array.toString()
    }
}