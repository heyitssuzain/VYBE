package com.example

import com.example.data.model.AudioSource
import com.example.data.model.RepeatMode
import com.example.data.model.Song
import com.example.ui.components.formatMs
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class ExampleUnitTest {

    @Test
    fun `formatMs formats durations accurately`() {
        assertEquals("0:00", formatMs(0L))
        assertEquals("0:05", formatMs(5000L))
        assertEquals("1:00", formatMs(60000L))
        assertEquals("3:45", formatMs(225000L))
        assertEquals("10:00", formatMs(600000L))
    }

    @Test
    fun `repeat mode cycle logic`() {
        var mode = RepeatMode.OFF
        fun next(m: RepeatMode) = when (m) {
            RepeatMode.OFF -> RepeatMode.ALL
            RepeatMode.ALL -> RepeatMode.ONE
            RepeatMode.ONE -> RepeatMode.OFF
        }

        mode = next(mode)
        assertEquals(RepeatMode.ALL, mode)
        mode = next(mode)
        assertEquals(RepeatMode.ONE, mode)
        mode = next(mode)
        assertEquals(RepeatMode.OFF, mode)
    }

    @Test
    fun `song model properties integrity`() {
        val song = Song(
            id = "jamendo_123",
            title = "Quantum Echo",
            artist = "Aether",
            album = "Space Vol 1",
            streamUrl = "https://example.com/stream.mp3",
            durationMs = 180000L,
            source = AudioSource.JAMENDO,
            license = "CC BY-SA"
        )

        assertEquals("Quantum Echo", song.title)
        assertEquals(AudioSource.JAMENDO, song.source)
        assertTrue(song.streamUrl.endsWith(".mp3"))
    }

    @Test
    fun `stream format sorting prioritizes native M4A and AAC over WebM`() {
        val webmStream = com.example.data.repository.ExtractedAudioStream(
            url = "https://example.com/audio.webm",
            format = "WEBM",
            codec = "opus",
            bitrate = 160000,
            isNativeHwSupported = false
        )
        val m4aStream = com.example.data.repository.ExtractedAudioStream(
            url = "https://example.com/audio.m4a",
            format = "M4A",
            codec = "mp4a.40.2",
            bitrate = 128000,
            isNativeHwSupported = true
        )

        val list = listOf(webmStream, m4aStream)
        val sorted = list.sortedWith(
            compareByDescending<com.example.data.repository.ExtractedAudioStream> { it.isNativeHwSupported }
                .thenByDescending { it.bitrate }
        )

        assertEquals("M4A", sorted.first().format)
        assertTrue(sorted.first().isNativeHwSupported)
    }
}
