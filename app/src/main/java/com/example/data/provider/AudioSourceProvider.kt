package com.example.data.provider

import com.example.data.model.AudioSource
import com.example.data.model.Song

interface AudioSourceProvider {
    val source: AudioSource

    suspend fun search(query: String, limit: Int = 20): List<Song>

    suspend fun getFeatured(): List<Song>

    suspend fun resolveStreamUrl(song: Song): String?
}
