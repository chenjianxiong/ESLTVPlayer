package com.xiaomi.tvplayer.ui.player

import android.app.AlertDialog
import android.net.Uri
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.KeyEvent
import android.view.View
import android.widget.Button
import android.widget.FrameLayout
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.google.android.exoplayer2.ExoPlayer
import com.google.android.exoplayer2.MediaItem
import com.google.android.exoplayer2.Player
import com.google.android.exoplayer2.ui.PlayerView
import com.xiaomi.tvplayer.R
import com.xiaomi.tvplayer.manager.FileManager
import com.xiaomi.tvplayer.manager.OverlayManager
import com.xiaomi.tvplayer.manager.PlaybackManager
import com.xiaomi.tvplayer.manager.SettingsManager
import kotlinx.coroutines.launch

/**
 * Video player activity with ExoPlayer
 */
class PlayerActivity : AppCompatActivity() {

    private lateinit var playerView: PlayerView
    private lateinit var playerContainer: FrameLayout

    private var player: ExoPlayer? = null
    private lateinit var settingsManager: SettingsManager
    private lateinit var playbackManager: PlaybackManager
    private lateinit var overlayManager: OverlayManager
    private lateinit var fileManager: FileManager

    private var filePath: String = ""
    private var savedPosition: Long = 0

    private val positionSaveHandler = Handler(Looper.getMainLooper())
    private val positionSaveRunnable = object : Runnable {
        override fun run() {
            saveCurrentPosition()
            positionSaveHandler.postDelayed(this, 5000) // Save every 5 seconds
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_player)

        initializeViews()
        initializeManagers()

        filePath = intent.getStringExtra("FILE_PATH") ?: ""
        if (filePath.isEmpty()) {
            Toast.makeText(this, R.string.error_file_not_found, Toast.LENGTH_SHORT).show()
            finish()
            return
        }

        checkResumePosition()
    }

    private fun initializeViews() {
        playerView = findViewById(R.id.playerView)
        playerContainer = findViewById(R.id.playerContainer)
    }

    private fun initializeManagers() {
        settingsManager = SettingsManager(this)
        playbackManager = PlaybackManager(this)
        overlayManager = OverlayManager(this)
        fileManager = FileManager()
    }

    private fun checkResumePosition() {
        lifecycleScope.launch {
            val record = playbackManager.getPlaybackPosition(filePath)
            if (record != null && record.positionMs > 5000) {
                // Show resume dialog
                showResumeDialog(record.positionMs)
            } else {
                // Start playback from beginning
                initializePlayer(0)
            }
        }
    }

    private fun showResumeDialog(positionMs: Long) {
        val dialogView = layoutInflater.inflate(R.layout.dialog_resume, null)
        val tvMessage = dialogView.findViewById<TextView>(R.id.tvResumeMessage)
        val btnResume = dialogView.findViewById<Button>(R.id.btnResume)
        val btnPlayFromStart = dialogView.findViewById<Button>(R.id.btnPlayFromStart)

        val timeStr = fileManager.formatDuration(positionMs)
        tvMessage.text = getString(R.string.resume_from, timeStr)
        btnResume.text = getString(R.string.resume_from, timeStr)

        val dialog = AlertDialog.Builder(this)
            .setView(dialogView)
            .setCancelable(false)
            .create()

        btnResume.setOnClickListener {
            dialog.dismiss()
            initializePlayer(positionMs)
        }

        btnPlayFromStart.setOnClickListener {
            dialog.dismiss()
            initializePlayer(0)
        }

        dialog.show()

        // Auto-select resume after 10 seconds
        Handler(Looper.getMainLooper()).postDelayed({
            if (dialog.isShowing) {
                dialog.dismiss()
                initializePlayer(positionMs)
            }
        }, 10000)
    }

    private fun initializePlayer(startPosition: Long) {
        player = ExoPlayer.Builder(this).build()
        playerView.player = player

        val mediaItem = MediaItem.fromUri(Uri.parse(filePath))
        player?.setMediaItem(mediaItem)
        player?.prepare()
        player?.seekTo(startPosition)
        player?.playWhenReady = true

        savedPosition = startPosition

        // Start position saving
        positionSaveHandler.post(positionSaveRunnable)

        // Initialize overlay
        val overlayConfig = settingsManager.getOverlayConfig()
        if (overlayConfig.enabled) {
            overlayManager.createOverlay(playerContainer, overlayConfig)
        }

        // Add player listener
        player?.addListener(object : Player.Listener {
            override fun onPlaybackStateChanged(state: Int) {
                if (state == Player.STATE_ENDED) {
                    onVideoCompleted()
                }
            }
        })
    }

    private fun saveCurrentPosition() {
        player?.let {
            val currentPos = it.currentPosition
            val duration = it.duration
            if (duration > 0) {
                lifecycleScope.launch {
                    playbackManager.savePlaybackPosition(filePath, currentPos, duration)
                }
            }
        }
    }

    private fun onVideoCompleted() {
        player?.let {
            val currentPos = it.currentPosition
            val duration = it.duration
            if (playbackManager.isVideoCompleted(currentPos, duration)) {
                // Clear position for completed video
                lifecycleScope.launch {
                    playbackManager.clearPlaybackPosition(filePath)
                }
            }
        }
    }

    override fun onKeyDown(keyCode: Int, event: KeyEvent?): Boolean {
        val settings = settingsManager.getAppSettings()

        return when (keyCode) {
            KeyEvent.KEYCODE_DPAD_LEFT -> {
                // Seek backward
                val seekAmount = settings.seekBackwardSeconds * 1000L
                player?.let {
                    val newPos = (it.currentPosition - seekAmount).coerceAtLeast(0)
                    it.seekTo(newPos)
                    Toast.makeText(
                        this,
                        getString(R.string.seek_backward, settings.seekBackwardSeconds),
                        Toast.LENGTH_SHORT
                    ).show()
                }
                true
            }
            KeyEvent.KEYCODE_DPAD_RIGHT -> {
                // Seek forward
                val seekAmount = settings.seekForwardSeconds * 1000L
                player?.let {
                    val newPos = (it.currentPosition + seekAmount).coerceAtMost(it.duration)
                    it.seekTo(newPos)
                    Toast.makeText(
                        this,
                        getString(R.string.seek_forward, settings.seekForwardSeconds),
                        Toast.LENGTH_SHORT
                    ).show()
                }
                true
            }
            KeyEvent.KEYCODE_DPAD_UP -> {
                // Show overlay
                overlayManager.showOverlay()
                Toast.makeText(this, R.string.overlay_shown, Toast.LENGTH_SHORT).show()
                true
            }
            KeyEvent.KEYCODE_DPAD_DOWN -> {
                // Hide overlay
                overlayManager.hideOverlay()
                Toast.makeText(this, R.string.overlay_hidden, Toast.LENGTH_SHORT).show()
                true
            }
            KeyEvent.KEYCODE_DPAD_CENTER, KeyEvent.KEYCODE_ENTER -> {
                // Play/Pause
                player?.let {
                    if (it.isPlaying) {
                        it.pause()
                    } else {
                        it.play()
                    }
                }
                true
            }
            KeyEvent.KEYCODE_BACK, KeyEvent.KEYCODE_HOME -> {
                // Save position and exit
                saveCurrentPosition()
                finish()
                true
            }
            else -> super.onKeyDown(keyCode, event)
        }
    }

    override fun onPause() {
        super.onPause()
        player?.pause()
    }

    override fun onDestroy() {
        super.onDestroy()
        positionSaveHandler.removeCallbacks(positionSaveRunnable)
        saveCurrentPosition()
        player?.release()
        player = null
    }
}
