package com.xiaomi.tvplayer.manager

import android.media.MediaMetadataRetriever
import android.os.Environment
import com.xiaomi.tvplayer.data.model.VideoFile
import java.io.File

/**
 * Manager for file system operations
 */
class FileManager {

    companion object {
        private val VIDEO_EXTENSIONS = setOf("mp4", "mkv", "avi", "mov", "webm", "flv")

        private val STORAGE_PATHS = listOf(
            "/sdcard/",
            "/storage/emulated/0/",
            Environment.getExternalStorageDirectory().absolutePath
        )
    }

    /**
     * Get list of video files and directories in the specified path
     */
    fun getFilesInDirectory(path: String): List<VideoFile> {
        val directory = File(path)
        if (!directory.exists() || !directory.isDirectory) {
            return emptyList()
        }

        val files = mutableListOf<VideoFile>()

        directory.listFiles()?.forEach { file ->
            if (file.isDirectory) {
                files.add(
                    VideoFile(
                        path = file.absolutePath,
                        name = file.name,
                        size = 0,
                        lastModified = file.lastModified(),
                        isDirectory = true
                    )
                )
            } else if (isVideoFile(file)) {
                files.add(
                    VideoFile(
                        path = file.absolutePath,
                        name = file.name,
                        size = file.length(),
                        duration = getVideoDuration(file.absolutePath),
                        lastModified = file.lastModified(),
                        isDirectory = false
                    )
                )
            }
        }

        // Sort: directories first, then by name
        return files.sortedWith(compareBy({ !it.isDirectory }, { it.name.lowercase() }))
    }

    /**
     * Check if file is a supported video file
     */
    private fun isVideoFile(file: File): Boolean {
        val extension = file.extension.lowercase()
        return VIDEO_EXTENSIONS.contains(extension)
    }

    /**
     * Get video duration in milliseconds
     */
    private fun getVideoDuration(path: String): Long? {
        return try {
            val retriever = MediaMetadataRetriever()
            retriever.setDataSource(path)
            val duration = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION)
            retriever.release()
            duration?.toLongOrNull()
        } catch (e: Exception) {
            null
        }
    }

    /**
     * Get all available storage locations
     */
    fun getStorageLocations(): List<String> {
        val locations = mutableListOf<String>()

        // Add standard storage paths that exist
        STORAGE_PATHS.forEach { path ->
            val file = File(path)
            if (file.exists() && file.isDirectory) {
                locations.add(path)
            }
        }

        // Check for USB drives
        val mntDir = File("/mnt/")
        if (mntDir.exists()) {
            mntDir.listFiles()?.forEach { file ->
                if (file.isDirectory && file.name.startsWith("usb", ignoreCase = true)) {
                    locations.add(file.absolutePath)
                }
            }
        }

        val storageDir = File("/storage/")
        if (storageDir.exists()) {
            storageDir.listFiles()?.forEach { file ->
                if (file.isDirectory && !file.name.equals("emulated", ignoreCase = true)
                    && !file.name.equals("self", ignoreCase = true)) {
                    locations.add(file.absolutePath)
                }
            }
        }

        return locations.distinct()
    }

    /**
     * Format file size to human readable string
     */
    fun formatFileSize(bytes: Long): String {
        val kb = 1024
        val mb = kb * 1024
        val gb = mb * 1024

        return when {
            bytes >= gb -> String.format("%.2f GB", bytes.toDouble() / gb)
            bytes >= mb -> String.format("%.2f MB", bytes.toDouble() / mb)
            bytes >= kb -> String.format("%.2f KB", bytes.toDouble() / kb)
            else -> "$bytes B"
        }
    }

    /**
     * Format duration to HH:MM:SS
     */
    fun formatDuration(milliseconds: Long): String {
        val seconds = (milliseconds / 1000) % 60
        val minutes = (milliseconds / (1000 * 60)) % 60
        val hours = milliseconds / (1000 * 60 * 60)

        return if (hours > 0) {
            String.format("%02d:%02d:%02d", hours, minutes, seconds)
        } else {
            String.format("%02d:%02d", minutes, seconds)
        }
    }
}
