package com.xiaomi.tvplayer.data.database

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.xiaomi.tvplayer.data.model.PlaybackRecord

/**
 * DAO for playback history operations
 */
@Dao
interface PlaybackDao {

    @Query("SELECT * FROM playback_history WHERE filePath = :filePath LIMIT 1")
    suspend fun getPlaybackRecord(filePath: String): PlaybackRecord?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPlaybackRecord(record: PlaybackRecord): Long

    @Update
    suspend fun updatePlaybackRecord(record: PlaybackRecord)

    @Query("DELETE FROM playback_history WHERE filePath = :filePath")
    suspend fun deletePlaybackRecord(filePath: String)

    @Query("SELECT * FROM playback_history ORDER BY lastPlayed DESC")
    suspend fun getAllPlaybackRecords(): List<PlaybackRecord>

    @Query("DELETE FROM playback_history")
    suspend fun clearAllRecords()
}
