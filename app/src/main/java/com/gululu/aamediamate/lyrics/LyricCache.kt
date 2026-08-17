package com.gululu.aamediamate.lyrics

import android.content.Context
import android.util.Log
import com.gululu.aamediamate.diagnostics.DiagnosticLogger
import com.gululu.aamediamate.diagnostics.DiagnosticModule
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File

object LyricCache {
    private val memoryCache = mutableMapOf<String, List<LyricLine>?>()
    private var lyricsDir: File? = null
    fun getLyricsDir(context: Context): File {
        if (lyricsDir == null)
        {
            val dir = context.getExternalFilesDir("lyrics")!!
            if (!dir.exists()) dir.mkdirs()
            lyricsDir = dir
        }

        return lyricsDir!!
    }

    private fun sanitizeFileName(input: String): String {
        return input.replace(Regex("[\\\\/:*?\"<>|]"), "_")
    }

    private fun getLyricFile(context: Context, title: String, artist: String): File {
        return File(getLyricsDir(context), "${sanitizeFileName(title)}_${sanitizeFileName(artist)}.lrt")
    }

    suspend fun getOrFetchLyrics(context: Context, title: String, artist: String, duration: String): List<LyricLine> {
        val key = title+"_" +artist
        Log.d("MediaBridge", "🎤 Getting lyrics for: $title by $artist")
        DiagnosticLogger.info(
            context,
            DiagnosticModule.LYRICS,
            "Getting lyrics",
            mapOf("title" to title, "artist" to artist, "duration" to duration)
        )

        val file = getLyricFile(context, title, artist)

        if (memoryCache.containsKey(key)) {
            Log.d("MediaBridge", "🎤 Reading from mem cache")
            DiagnosticLogger.info(
                context,
                DiagnosticModule.LYRICS,
                "Lyric memory cache hit",
                mapOf("title" to title, "artist" to artist)
            )
            val cached = memoryCache[key]
            if (file.exists()) {
                file.setLastModified(System.currentTimeMillis())
            }
            return cached ?: emptyList()
        }

        if (file.exists()) {
            val lrcContent = file.readText()
            Log.d("MediaBridge", "🎤 Reading from file: $file")
            DiagnosticLogger.info(
                context,
                DiagnosticModule.LYRICS,
                "Lyric file cache hit",
                mapOf("title" to title, "artist" to artist, "bytes" to lrcContent.toByteArray().size)
            )

            if (lrcContent.isBlank()) {
                Log.d("MediaBridge", "🎤 Reading from file, but no lyrics found.")
                DiagnosticLogger.info(
                    context,
                    DiagnosticModule.LYRICS,
                    "Cached lyric miss marker found",
                    mapOf("title" to title, "artist" to artist)
                )
                memoryCache[key] = null
                file.setLastModified(System.currentTimeMillis())
                return emptyList()
            }

            val lyrics = LyricsManager.parseLrc(context, lrcContent)
            memoryCache[key] = lyrics
            Log.d("MediaBridge", "🎤 Loaded ${lyrics.size} lines from cache")
            DiagnosticLogger.info(
                context,
                DiagnosticModule.LYRICS,
                "Parsed cached lyrics",
                mapOf("title" to title, "artist" to artist, "lineCount" to lyrics.size)
            )
            file.setLastModified(System.currentTimeMillis())
            return lyrics
        }

        Log.d("MediaBridge", "🎤 Fetching lyrics from network...")
        DiagnosticLogger.info(
            context,
            DiagnosticModule.LYRICS,
            "Fetching lyrics from providers",
            mapOf("title" to title, "artist" to artist)
        )
        val lrcContent = LyricsManager.getLyricsLrt(context, title, artist, duration)
        Log.d("MediaBridge", "🎤 Network fetch returned ${lrcContent?.toByteArray()?.size ?: 0} byte(s)")
        if (!lrcContent.isNullOrBlank()) {
            val lyrics = LyricsManager.parseLrc(context, lrcContent)
            Log.d("MediaBridge", "🎤 Parsed ${lyrics.size} lyric line(s)")
            if (lyrics.isNotEmpty()) {
                memoryCache[key] = lyrics
                file.writeText(lrcContent)
                Log.d("MediaBridge", "🎤 Saved ${lyrics.size} lines to file")
                DiagnosticLogger.info(
                    context,
                    DiagnosticModule.LYRICS,
                    "Fetched and cached lyrics",
                    mapOf("title" to title, "artist" to artist, "lineCount" to lyrics.size)
                )
                return lyrics
            } else {
                memoryCache[key] = null
                withContext(Dispatchers.IO) {
                    file.createNewFile()
                }
                Log.d("MediaBridge", "🎤 Failed to parse lyrics")
                DiagnosticLogger.warn(
                    context,
                    DiagnosticModule.LYRICS,
                    "Fetched lyrics could not be parsed",
                    mapOf("title" to title, "artist" to artist, "bytes" to lrcContent.toByteArray().size)
                )
                return emptyList()
            }
        } else {
            memoryCache[key] = null
            withContext(Dispatchers.IO) {
                file.createNewFile()
            }
            Log.d("MediaBridge", "🎤 No lyrics found from network")
            DiagnosticLogger.warn(
                context,
                DiagnosticModule.LYRICS,
                "No lyrics found from providers",
                mapOf("title" to title, "artist" to artist)
            )
            return emptyList()
        }
    }

    fun clearMemoryCache(key: String) {
        memoryCache.remove(key)
    }
}
