package com.xiaomi.tvplayer.manager

import android.content.Context
import android.graphics.Color
import android.view.Gravity
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import com.xiaomi.tvplayer.data.model.OverlayConfig

/**
 * Manager for overlay functionality
 */
class OverlayManager(private val context: Context) {

    private var overlayView: View? = null
    private var isVisible = false

    /**
     * Create overlay view with the given configuration
     */
    fun createOverlay(parent: ViewGroup, config: OverlayConfig): View {
        // Remove existing overlay if any
        removeOverlay(parent)

        val overlay = View(context).apply {
            layoutParams = FrameLayout.LayoutParams(
                config.width,
                config.height
            ).apply {
                // If position is -1, default to Bottom-Left
                if (config.positionX == -1 && config.positionY == -1) {
                    gravity = Gravity.BOTTOM or Gravity.START
                    leftMargin = 50 // small margin from edge
                    bottomMargin = 50
                } else {
                    gravity = Gravity.TOP or Gravity.START
                    leftMargin = if (config.positionX >= 0) config.positionX else 0
                    topMargin = if (config.positionY >= 0) config.positionY else 0
                }
            }

            // Apply color with opacity
            val alpha = (config.opacity * 255 / 100).coerceIn(0, 255)
            val colorWithAlpha = Color.argb(
                alpha,
                Color.red(config.color),
                Color.green(config.color),
                Color.blue(config.color)
            )
            setBackgroundColor(colorWithAlpha)

            // Initially hidden
            visibility = View.GONE
        }

        parent.addView(overlay)
        overlayView = overlay
        isVisible = false

        return overlay
    }

    /**
     * Show overlay
     */
    fun showOverlay() {
        overlayView?.visibility = View.VISIBLE
        isVisible = true
    }

    /**
     * Hide overlay
     */
    fun hideOverlay() {
        overlayView?.visibility = View.GONE
        isVisible = false
    }

    /**
     * Toggle overlay visibility
     */
    fun toggleOverlay(): Boolean {
        if (isVisible) {
            hideOverlay()
        } else {
            showOverlay()
        }
        return isVisible
    }

    /**
     * Update overlay configuration
     */
    fun updateOverlay(parent: ViewGroup, config: OverlayConfig) {
        createOverlay(parent, config)
        if (isVisible) {
            showOverlay()
        }
    }

    /**
     * Remove overlay from parent
     */
    fun removeOverlay(parent: ViewGroup) {
        overlayView?.let {
            parent.removeView(it)
        }
        overlayView = null
        isVisible = false
    }

    /**
     * Check if overlay is currently visible
     */
    fun isOverlayVisible(): Boolean = isVisible
}
