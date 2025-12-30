package com.xiaomi.tvplayer.data.model

/**
 * View mode for the file browser
 */
enum class ViewMode {
    GRID,
    LIST
}

/**
 * Application settings
 */
data class AppSettings(
    val seekBackwardSeconds: Int = 5,
    val seekForwardSeconds: Int = 5,
    val overlayConfig: OverlayConfig = OverlayConfig(),
    val browserViewMode: ViewMode = ViewMode.GRID,
    val defaultDirectory: String = "/sdcard/",
    val showFileSize: Boolean = true,
    val showDuration: Boolean = true,
    val scanExternalStorage: Boolean = true,
    val directoryFilter: String = "" // New filter for directory names
)
