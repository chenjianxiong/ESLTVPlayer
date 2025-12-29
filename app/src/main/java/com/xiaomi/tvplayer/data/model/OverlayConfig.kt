package com.xiaomi.tvplayer.data.model

import android.graphics.Color

/**
 * Data class representing overlay configuration
 */
data class OverlayConfig(
    val enabled: Boolean = true,
    val color: Int = Color.RED,
    val width: Int = 200,
    val height: Int = 100,
    val positionX: Int = -1, // -1 for center
    val positionY: Int = -1, // -1 for center
    val opacity: Int = 80 // 0-100
)
