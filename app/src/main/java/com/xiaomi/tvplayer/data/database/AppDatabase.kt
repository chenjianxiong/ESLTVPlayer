package com.xiaomi.tvplayer.data.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.xiaomi.tvplayer.data.model.PlaybackRecord

/**
 * Room database for the application
 */
@Database(entities = [PlaybackRecord::class], version = 1, exportSchema = false)
abstract class AppDatabase : RoomDatabase() {

    abstract fun playbackDao(): PlaybackDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "xiaomi_video_player_database"
                ).build()
                INSTANCE = instance
                instance
            }
        }
    }
}
