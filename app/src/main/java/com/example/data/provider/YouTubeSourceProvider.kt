package com.example.data.provider

import com.example.data.model.AudioSource
import com.example.data.model.Song
import com.example.data.remote.NetworkClient
import com.example.data.repository.YouTubeStreamRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONArray
import org.json.JSONObject
import java.net.URLEncoder

/**
 * High-performance YouTube streaming audio provider.
 * Uses native YouTube web search for instant, accurate results with official HD thumbnails,
 * with multi-instance Piped API and yt-dlp compatible Invidious fallback.
 */
class YouTubeSourceProvider : AudioSourceProvider {
    override val source: AudioSource = AudioSource.YOUTUBE

    private val youtubeRepository = YouTubeStreamRepository()

    private val pipedInstances = listOf(
        "https://pipedapi.kavin.rocks",
        "https://api.piped.privacydev.net",
        "https://piped-api.lunar.icu",
        "https://pipedapi.tokhmi.xyz",
        "https://api-piped.mha.fi"
    )

    private val invidiousInstances = listOf(
        "https://inv.tux.pizza",
        "https://invidious.nerdvpn.de",
        "https://vid.puffyan.us",
        "https://invidious.projectsegfau.lt",
        "https://yewtu.be"
    )

    override suspend fun search(query: String, limit: Int): List<Song> = withContext(Dispatchers.IO) {
        val cleanQuery = query.trim()
        if (cleanQuery.isBlank()) return@withContext emptyList()

        // 1. Direct YouTube Web Search Parser (fastest, most accurate, real official thumbnails)
        val directYtResults = searchYouTubeDirect(cleanQuery, limit)
        if (directYtResults.isNotEmpty()) {
            return@withContext directYtResults
        }

        // 2. Fallback to Piped API search
        val encoded = try { URLEncoder.encode(cleanQuery, "UTF-8") } catch (e: Exception) { cleanQuery }
        val pipedResults = searchPiped(encoded, limit)
        if (pipedResults.isNotEmpty()) {
            return@withContext pipedResults
        }

        // 3. Fallback to Invidious search (yt-dlp compatible)
        searchInvidious(encoded, limit)
    }

    /**
     * Direct YouTube search scraper matching official YouTube results, video IDs, and HD artwork.
     */
    private fun searchYouTubeDirect(query: String, limit: Int): List<Song> {
        return try {
            val encoded = URLEncoder.encode(query, "UTF-8")
            val url = "https://www.youtube.com/results?search_query=$encoded&hl=en"
            val html = NetworkClient.getRawString(url) ?: return emptyList()

            val pattern = java.util.regex.Pattern.compile("var ytInitialData = (\\{.+?\\});</script>")
            val matcher = pattern.matcher(html)
            if (!matcher.find()) return emptyList()

            val jsonStr = matcher.group(1) ?: return emptyList()
            val root = JSONObject(jsonStr)

            val songs = mutableListOf<Song>()
            findVideoRenderers(root, songs, limit)
            songs
        } catch (e: Exception) {
            emptyList()
        }
    }

    private fun findVideoRenderers(obj: Any?, out: MutableList<Song>, limit: Int) {
        if (out.size >= limit) return

        when (obj) {
            is JSONObject -> {
                if (obj.has("videoRenderer")) {
                    val vr = obj.optJSONObject("videoRenderer")
                    if (vr != null) {
                        val videoId = vr.optString("videoId")
                        val titleObj = vr.optJSONObject("title")
                        val titleRuns = titleObj?.optJSONArray("runs")
                        val title = titleRuns?.optJSONObject(0)?.optString("text")
                            ?: titleObj?.optString("simpleText", "Unknown Song") ?: "Unknown Song"

                        val ownerObj = vr.optJSONObject("ownerText")
                        val ownerRuns = ownerObj?.optJSONArray("runs")
                        val channel = ownerRuns?.optJSONObject(0)?.optString("text") ?: "YouTube Music"

                        val durationText = vr.optJSONObject("lengthText")?.optString("simpleText")
                        val durationMs = parseDurationMs(durationText)

                        // High resolution thumbnail
                        val thumbs = vr.optJSONObject("thumbnail")?.optJSONArray("thumbnails")
                        val thumbUrl = if (thumbs != null && thumbs.length() > 0) {
                            thumbs.optJSONObject(thumbs.length() - 1)?.optString("url")
                                ?: "https://i.ytimg.com/vi/$videoId/hqdefault.jpg"
                        } else {
                            "https://i.ytimg.com/vi/$videoId/hqdefault.jpg"
                        }

                        if (videoId.isNotBlank() && title.isNotBlank()) {
                            out.add(
                                Song(
                                    id = "yt_$videoId",
                                    title = title,
                                    artist = channel,
                                    album = "YouTube Music",
                                    artworkUrl = thumbUrl,
                                    streamUrl = "https://inv.tux.pizza/latest_version?id=$videoId&itag=140",
                                    durationMs = durationMs,
                                    source = AudioSource.YOUTUBE,
                                    license = "YouTube Audio Stream"
                                )
                            )
                        }
                    }
                }

                val keys = obj.keys()
                while (keys.hasNext()) {
                    val key = keys.next()
                    findVideoRenderers(obj.opt(key), out, limit)
                    if (out.size >= limit) return
                }
            }
            is JSONArray -> {
                for (i in 0 until obj.length()) {
                    findVideoRenderers(obj.opt(i), out, limit)
                    if (out.size >= limit) return
                }
            }
        }
    }

    private fun parseDurationMs(text: String?): Long {
        if (text.isNullOrBlank()) return 180000L
        val parts = text.split(":")
        var sec = 0L
        for (part in parts) {
            sec = sec * 60 + (part.toLongOrNull() ?: 0L)
        }
        return sec * 1000L
    }

    private fun searchPiped(encodedQuery: String, limit: Int): List<Song> {
        for (instance in pipedInstances) {
            try {
                val url = "$instance/search?q=$encodedQuery&filter=music_songs"
                val json = NetworkClient.getJson(url) ?: continue
                val items = json.optJSONArray("items") ?: continue

                val songs = mutableListOf<Song>()
                val count = minOf(items.length(), limit)

                for (i in 0 until count) {
                    val item = items.optJSONObject(i) ?: continue
                    val urlPath = item.optString("url")
                    val videoId = if (urlPath.contains("v=")) {
                        urlPath.substringAfter("v=").substringBefore("&")
                    } else {
                        urlPath.removePrefix("/watch?v=")
                    }
                    if (videoId.isBlank()) continue

                    val title = item.optString("title", "Unknown Track")
                    val uploaderName = item.optString("uploaderName", "Online Artist")
                    val durationSec = item.optLong("duration", 0L)
                    val thumbnail = item.optString("thumbnail").ifBlank {
                        "https://i.ytimg.com/vi/$videoId/hqdefault.jpg"
                    }

                    songs.add(
                        Song(
                            id = "yt_$videoId",
                            title = title,
                            artist = uploaderName,
                            album = "YouTube Music",
                            artworkUrl = thumbnail,
                            streamUrl = "$instance/streams/$videoId",
                            durationMs = durationSec * 1000L,
                            source = AudioSource.YOUTUBE,
                            license = "Direct Audio Stream (Piped)"
                        )
                    )
                }

                if (songs.isNotEmpty()) return songs
            } catch (e: Exception) {}
        }
        return emptyList()
    }

    private fun searchInvidious(encodedQuery: String, limit: Int): List<Song> {
        for (instance in invidiousInstances) {
            try {
                val url = "$instance/api/v1/search?q=$encodedQuery&type=video"
                val raw = NetworkClient.getRawString(url) ?: continue
                val array = try { JSONArray(raw) } catch (e: Exception) { null } ?: continue

                val songs = mutableListOf<Song>()
                val count = minOf(array.length(), limit)

                for (i in 0 until count) {
                    val item = array.optJSONObject(i) ?: continue
                    val videoId = item.optString("videoId")
                    if (videoId.isBlank()) continue

                    val title = item.optString("title", "Unknown Track")
                    val author = item.optString("author", "Online Artist")
                    val durationSec = item.optLong("lengthSeconds", 0L)
                    val thumbnails = item.optJSONArray("videoThumbnails")
                    val thumbUrl = thumbnails?.optJSONObject(0)?.optString("url")
                        ?: "https://i.ytimg.com/vi/$videoId/hqdefault.jpg"

                    songs.add(
                        Song(
                            id = "yt_$videoId",
                            title = title,
                            artist = author,
                            album = "YouTube Music",
                            artworkUrl = thumbUrl,
                            streamUrl = "$instance/latest_version?id=$videoId&itag=140",
                            durationMs = durationSec * 1000L,
                            source = AudioSource.YOUTUBE,
                            license = "Direct Audio Stream (Invidious M4A)"
                        )
                    )
                }

                if (songs.isNotEmpty()) return songs
            } catch (e: Exception) {}
        }
        return emptyList()
    }

    override suspend fun getFeatured(): List<Song> = withContext(Dispatchers.IO) {
        // Return a few popular curated tracks for initial showcase
        listOf(
            Song(
                id = "yt_dQw4w9WgXcQ",
                title = "Never Gonna Give You Up",
                artist = "Rick Astley",
                album = "YouTube Music",
                artworkUrl = "https://i.ytimg.com/vi/dQw4w9WgXcQ/hqdefault.jpg",
                streamUrl = "https://inv.tux.pizza/latest_version?id=dQw4w9WgXcQ&itag=140",
                durationMs = 213000L,
                source = AudioSource.YOUTUBE,
                license = "YouTube Audio Stream"
            ),
            Song(
                id = "yt_9bZkp7q19f0",
                title = "Gangnam Style",
                artist = "PSY",
                album = "YouTube Music",
                artworkUrl = "https://i.ytimg.com/vi/9bZkp7q19f0/hqdefault.jpg",
                streamUrl = "https://inv.tux.pizza/latest_version?id=9bZkp7q19f0&itag=140",
                durationMs = 253000L,
                source = AudioSource.YOUTUBE,
                license = "YouTube Audio Stream"
            ),
            Song(
                id = "yt_kJQP7kiw5Fk",
                title = "Despacito",
                artist = "Luis Fonsi ft. Daddy Yankee",
                album = "YouTube Music",
                artworkUrl = "https://i.ytimg.com/vi/kJQP7kiw5Fk/hqdefault.jpg",
                streamUrl = "https://inv.tux.pizza/latest_version?id=kJQP7kiw5Fk&itag=140",
                durationMs = 282000L,
                source = AudioSource.YOUTUBE,
                license = "YouTube Audio Stream"
            )
        )
    }

    override suspend fun resolveStreamUrl(song: Song): String? = withContext(Dispatchers.IO) {
        val videoId = song.id.removePrefix("yt_")
        youtubeRepository.resolveStreamUrlParallelRace(song)
    }
}