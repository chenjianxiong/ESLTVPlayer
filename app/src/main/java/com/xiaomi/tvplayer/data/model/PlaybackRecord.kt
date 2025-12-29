package com.xiaomi.tvplayer.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * Entity representing a playback position record in the database
 */
@Entity(tableName = "playback_history")
data class PlaybackRecord(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val filePath: String,
    val positionMs: Long,
    val durationMs: Long,
    val lastPlayed: Long = System.currentTimeMillis()
)
