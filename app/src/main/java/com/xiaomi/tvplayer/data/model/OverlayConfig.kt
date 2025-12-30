package com.xiaomi.tvplayer.data.model

import android.graphics.Color

/**
 * Data class representing overlay configuration
 */
data class OverlayConfig(
    val enabled: Boolean = true,
    val color: Int = Color.BLACK,
    val width: Int = 1400, // New default width
    val height: Int = 55,   // New default height
    val positionX: Int = 380, // New default X
    val positionY: Int = 940, // New default Y
    val opacity: Int = 100
)
