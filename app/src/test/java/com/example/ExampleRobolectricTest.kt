package com.example

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.example.data.model.AudioSource
import com.example.data.model.RepeatMode
import com.example.data.model.Song
import com.example.data.repository.MusicRepository
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
class ExampleRobolectricTest {

    @Test
    fun `read string from context matches app name`() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val appName = context.getString(R.string.app_name)
        assertEquals("Aether Music", appName)
    }

    @Test
    fun `music repository manages playlists correctly`() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val repository = MusicRepository(context)

        val pl = repository.createPlaylist("Test Ambient", "Space ambient sounds")
        assertNotNull(pl.id)
        assertEquals("Test Ambient", pl.name)

        repository.addSongToPlaylist(pl.id, "song_123")
        val updatedPl = repository.playlists.value.find { it.id == pl.id }
        assertNotNull(updatedPl)
        assertTrue(updatedPl!!.songIds.contains("song_123"))

        repository.renamePlaylist(pl.id, "Renamed Space")
        val renamed = repository.playlists.value.find { it.id == pl.id }
        assertEquals("Renamed Space", renamed?.name)

        repository.deletePlaylist(pl.id)
        assertFalse(repository.playlists.value.any { it.id == pl.id })
    }

    @Test
    fun `favorite toggle works and persists`() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val repository = MusicRepository(context)
        val dummySong = Song(
            id = "test_track_1",
            title = "Test Starlight",
            artist = "Solaris",
            streamUrl = "https://example.com/audio.mp3",
            source = AudioSource.JAMENDO
        )

        assertFalse(repository.isFavorite(dummySong.id))
        repository.toggleFavorite(dummySong)
        assertTrue(repository.isFavorite(dummySong.id))
        repository.toggleFavorite(dummySong)
        assertFalse(repository.isFavorite(dummySong.id))
    }
}
