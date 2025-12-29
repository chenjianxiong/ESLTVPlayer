package com.xiaomi.tvplayer.manager

import android.content.Context
import android.content.SharedPreferences
import com.google.gson.Gson
import com.xiaomi.tvplayer.data.model.AppSettings
import com.xiaomi.tvplayer.data.model.OverlayConfig

/**
 * Manager for application settings using SharedPreferences
 */
class SettingsManager(context: Context) {

    private val prefs: SharedPreferences = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
    private val gson = Gson()

    companion object {
        private const val PREFS_NAME = "xiaomi_video_player_prefs"
        private const val KEY_SEEK_BACKWARD = "seek_backward_seconds"
        private const val KEY_SEEK_FORWARD = "seek_forward_seconds"
        private const val KEY_OVERLAY_CONFIG = "overlay_config"
        private const val KEY_VIEW_MODE = "view_mode"
        private const val KEY_DEFAULT_DIR = "default_directory"
        private const val KEY_SHOW_FILE_SIZE = "show_file_size"
        private const val KEY_SHOW_DURATION = "show_duration"
        private const val KEY_SCAN_EXTERNAL = "scan_external_storage"
        private const val KEY_LAST_DIRECTORY = "last_directory"
    }

    fun getAppSettings(): AppSettings {
        val overlayJson = prefs.getString(KEY_OVERLAY_CONFIG, null)
        val overlayConfig = if (overlayJson != null) {
            gson.fromJson(overlayJson, OverlayConfig::class.java)
        } else {
            OverlayConfig()
        }

        return AppSettings(
            seekBackwardSeconds = prefs.getInt(KEY_SEEK_BACKWARD, 5),
            seekForwardSeconds = prefs.getInt(KEY_SEEK_FORWARD, 5),
            overlayConfig = overlayConfig,
            browserViewMode = com.xiaomi.tvplayer.data.model.ViewMode.valueOf(
                prefs.getString(KEY_VIEW_MODE, "GRID") ?: "GRID"
            ),
            defaultDirectory = prefs.getString(KEY_DEFAULT_DIR, "/sdcard/") ?: "/sdcard/",
            showFileSize = prefs.getBoolean(KEY_SHOW_FILE_SIZE, true),
            showDuration = prefs.getBoolean(KEY_SHOW_DURATION, true),
            scanExternalStorage = prefs.getBoolean(KEY_SCAN_EXTERNAL, true)
        )
    }

    fun saveAppSettings(settings: AppSettings) {
        prefs.edit().apply {
            putInt(KEY_SEEK_BACKWARD, settings.seekBackwardSeconds)
            putInt(KEY_SEEK_FORWARD, settings.seekForwardSeconds)
            putString(KEY_OVERLAY_CONFIG, gson.toJson(settings.overlayConfig))
            putString(KEY_VIEW_MODE, settings.browserViewMode.name)
            putString(KEY_DEFAULT_DIR, settings.defaultDirectory)
            putBoolean(KEY_SHOW_FILE_SIZE, settings.showFileSize)
            putBoolean(KEY_SHOW_DURATION, settings.showDuration)
            putBoolean(KEY_SCAN_EXTERNAL, settings.scanExternalStorage)
            apply()
        }
    }

    fun getSeekBackwardSeconds(): Int = prefs.getInt(KEY_SEEK_BACKWARD, 5)

    fun getSeekForwardSeconds(): Int = prefs.getInt(KEY_SEEK_FORWARD, 5)

    fun getOverlayConfig(): OverlayConfig {
        val json = prefs.getString(KEY_OVERLAY_CONFIG, null)
        return if (json != null) {
            gson.fromJson(json, OverlayConfig::class.java)
        } else {
            OverlayConfig()
        }
    }

    fun saveOverlayConfig(config: OverlayConfig) {
        prefs.edit().putString(KEY_OVERLAY_CONFIG, gson.toJson(config)).apply()
    }

    fun getLastDirectory(): String {
        return prefs.getString(KEY_LAST_DIRECTORY, "/sdcard/") ?: "/sdcard/"
    }

    fun saveLastDirectory(path: String) {
        prefs.edit().putString(KEY_LAST_DIRECTORY, path).apply()
    }
}
