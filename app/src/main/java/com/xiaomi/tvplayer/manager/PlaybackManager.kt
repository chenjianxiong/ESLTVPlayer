package com.xiaomi.tvplayer.manager

import android.content.Context
import com.xiaomi.tvplayer.data.database.AppDatabase
import com.xiaomi.tvplayer.data.model.PlaybackRecord
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/**
 * Manager for playback operations and position tracking
 */
class PlaybackManager(context: Context) {

    private val database = AppDatabase.getDatabase(context)
    private val playbackDao = database.playbackDao()

    /**
     * Save playback position
     */
    suspend fun savePlaybackPosition(filePath: String, positionMs: Long, durationMs: Long) {
        withContext(Dispatchers.IO) {
            val existingRecord = playbackDao.getPlaybackRecord(filePath)
            if (existingRecord != null) {
                playbackDao.updatePlaybackRecord(
                    existingRecord.copy(
                        positionMs = positionMs,
                        durationMs = durationMs,
                        lastPlayed = System.currentTimeMillis()
                    )
                )
            } else {
                playbackDao.insertPlaybackRecord(
                    PlaybackRecord(
                        filePath = filePath,
                        positionMs = positionMs,
                        durationMs = durationMs
                    )
                )
            }
        }
    }

    /**
     * Get playback position for a file
     */
    suspend fun getPlaybackPosition(filePath: String): PlaybackRecord? {
        return withContext(Dispatchers.IO) {
            playbackDao.getPlaybackRecord(filePath)
        }
    }

    /**
     * Clear playback position (when video is completed)
     */
    suspend fun clearPlaybackPosition(filePath: String) {
        withContext(Dispatchers.IO) {
            playbackDao.deletePlaybackRecord(filePath)
        }
    }

    /**
     * Check if video has been watched (>95%)
     */
    fun isVideoCompleted(positionMs: Long, durationMs: Long): Boolean {
        if (durationMs <= 0) return false
        val percentage = (positionMs.toDouble() / durationMs) * 100
        return percentage >= 95
    }

    /**
     * Get all playback history
     */
    suspend fun getAllPlaybackHistory(): List<PlaybackRecord> {
        return withContext(Dispatchers.IO) {
            playbackDao.getAllPlaybackRecords()
        }
    }
}
