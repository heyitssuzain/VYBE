package com.example.data.provider

import android.content.ContentUris
import android.content.Context
import android.media.MediaMetadataRetriever
import android.net.Uri
import android.os.Build
import android.provider.MediaStore
import android.provider.OpenableColumns
import com.example.data.model.AudioSource
import com.example.data.model.Song
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class LocalAudioSourceProvider(private val context: Context) : AudioSourceProvider {
    override val source: AudioSource = AudioSource.LOCAL

    override suspend fun search(query: String, limit: Int): List<Song> = withContext(Dispatchers.IO) {
        val allLocal = scanLocalLibrary()
        if (query.isBlank()) {
            allLocal.take(limit)
        } else {
            allLocal.filter {
                it.title.contains(query, ignoreCase = true) ||
                it.artist.contains(query, ignoreCase = true) ||
                it.album.contains(query, ignoreCase = true)
            }.take(limit)
        }
    }

    override suspend fun getFeatured(): List<Song> = withContext(Dispatchers.IO) {
        scanLocalLibrary().take(20)
    }

    override suspend fun resolveStreamUrl(song: Song): String? {
        return song.streamUrl
    }

    suspend fun scanLocalLibrary(): List<Song> = withContext(Dispatchers.IO) {
        val songs = mutableListOf<Song>()
        val collection = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            MediaStore.Audio.Media.getContentUri(MediaStore.VOLUME_EXTERNAL)
        } else {
            MediaStore.Audio.Media.EXTERNAL_CONTENT_URI
        }

        val projection = arrayOf(
            MediaStore.Audio.Media._ID,
            MediaStore.Audio.Media.TITLE,
            MediaStore.Audio.Media.ARTIST,
            MediaStore.Audio.Media.ALBUM,
            MediaStore.Audio.Media.DURATION,
            MediaStore.Audio.Media.ALBUM_ID
        )

        val selection = "${MediaStore.Audio.Media.IS_MUSIC} != 0"
        val sortOrder = "${MediaStore.Audio.Media.TITLE} ASC"

        try {
            context.contentResolver.query(
                collection,
                projection,
                selection,
                null,
                sortOrder
            )?.use { cursor ->
                val idCol = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media._ID)
                val titleCol = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.TITLE)
                val artistCol = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.ARTIST)
                val albumCol = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.ALBUM)
                val durationCol = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.DURATION)
                val albumIdCol = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.ALBUM_ID)

                while (cursor.moveToNext()) {
                    val id = cursor.getLong(idCol)
                    val title = cursor.getString(titleCol) ?: "Unknown Track"
                    val artist = cursor.getString(artistCol) ?: "Unknown Artist"
                    val album = cursor.getString(albumCol) ?: ""
                    val duration = cursor.getLong(durationCol)
                    val albumId = cursor.getLong(albumIdCol)

                    val contentUri = ContentUris.withAppendedId(MediaStore.Audio.Media.EXTERNAL_CONTENT_URI, id)
                    val artworkUri = ContentUris.withAppendedId(
                        Uri.parse("content://media/external/audio/albumart"),
                        albumId
                    ).toString()

                    songs.add(
                        Song(
                            id = "local_$id",
                            title = title,
                            artist = if (artist == "<unknown>") "Local Artist" else artist,
                            album = album,
                            artworkUrl = artworkUri,
                            streamUrl = contentUri.toString(),
                            durationMs = duration,
                            source = AudioSource.LOCAL,
                            license = "User-Owned Audio File"
                        )
                    )
                }
            }
        } catch (e: Exception) {
            // Permission not granted or query error
        }

        songs
    }

    suspend fun parseFromSafUri(uri: Uri): Song = withContext(Dispatchers.IO) {
        var displayName = "Imported Audio"
        var durationMs = 0L
        var artist = "Local File"

        try {
            context.contentResolver.query(uri, null, null, null, null)?.use { cursor ->
                if (cursor.moveToFirst()) {
                    val nameIndex = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME)
                    if (nameIndex != -1) {
                        displayName = cursor.getString(nameIndex) ?: displayName
                    }
                }
            }

            // Extract metadata via MediaMetadataRetriever
            val retriever = MediaMetadataRetriever()
            retriever.setDataSource(context, uri)
            val extractedTitle = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_TITLE)
            val extractedArtist = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_ARTIST)
            val extractedDur = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION)
            if (!extractedTitle.isNullOrBlank()) displayName = extractedTitle
            if (!extractedArtist.isNullOrBlank()) artist = extractedArtist
            if (!extractedDur.isNullOrBlank()) durationMs = extractedDur.toLongOrNull() ?: 0L
            retriever.release()
        } catch (e: Exception) {
            // Fallback
        }

        Song(
            id = "saf_${uri.hashCode()}",
            title = displayName.removeSuffix(".mp3").removeSuffix(".flac").removeSuffix(".m4a").removeSuffix(".wav"),
            artist = artist,
            album = "Imported Files",
            streamUrl = uri.toString(),
            durationMs = durationMs,
            source = AudioSource.LOCAL,
            license = "User-Owned Local File"
        )
    }
}
