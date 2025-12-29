package com.xiaomi.tvplayer.data.model

/**
 * Represents a video file in the file system
 */
data class VideoFile(
    val path: String,
    val name: String,
    val size: Long,
    val duration: Long? = null,
    val lastModified: Long,
    val isDirectory: Boolean
)
