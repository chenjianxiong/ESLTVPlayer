package com.xiaomi.tvplayer.ui.player

import android.app.AlertDialog
import android.net.Uri
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.KeyEvent
import android.view.WindowManager
import android.widget.Button
import android.widget.FrameLayout
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.google.android.exoplayer2.ExoPlayer
import com.google.android.exoplayer2.MediaItem
import com.google.android.exoplayer2.Player
import com.google.android.exoplayer2.ui.StyledPlayerView
import com.xiaomi.tvplayer.R
import com.xiaomi.tvplayer.data.model.AppSettings
import com.xiaomi.tvplayer.manager.FileManager
import com.xiaomi.tvplayer.manager.OverlayManager
import com.xiaomi.tvplayer.manager.PlaybackManager
import com.xiaomi.tvplayer.manager.SettingsManager
import kotlinx.coroutines.launch

/**
 * Video player activity with ExoPlayer
 */
class PlayerActivity : AppCompatActivity() {

    private val TAG = "PlayerActivity"
    private lateinit var playerView: StyledPlayerView
    private lateinit var playerContainer: FrameLayout

    private var player: ExoPlayer? = null
    private lateinit var settingsManager: SettingsManager
    private lateinit var playbackManager: PlaybackManager
    private lateinit var overlayManager: OverlayManager
    private lateinit var fileManager: FileManager
    
    private var appSettings: AppSettings? = null

    private var filePath: String = ""
    private var savedPosition: Long = 0
    
    private var lastSeekTime: Long = 0
    private val SEEK_DEBOUNCE_MS = 500L // Prevent accidental double-seeks within 0.5s

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

        // Disable screen saver while playing video
        window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)

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
        
        // Cache settings and log the values to debug huge jumps
        appSettings = settingsManager.getAppSettings()
        Log.d(TAG, "Initialized with SeekBack: ${appSettings?.seekBackwardSeconds}s, SeekForward: ${appSettings?.seekForwardSeconds}s")
    }

    private fun checkResumePosition() {
        lifecycleScope.launch {
            val record = playbackManager.getPlaybackPosition(filePath)
            if (record != null && record.positionMs > 5000) {
                showResumeDialog(record.positionMs)
            } else {
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
        positionSaveHandler.post(positionSaveRunnable)

        val overlayConfig = appSettings?.overlayConfig ?: settingsManager.getOverlayConfig()
        if (overlayConfig.enabled) {
            overlayManager.createOverlay(playerContainer, overlayConfig)
        }

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

    /**
     * Using dispatchKeyEvent to intercept events before they reach the StyledPlayerView
     */
    override fun dispatchKeyEvent(event: KeyEvent): Boolean {
        if (event.action == KeyEvent.ACTION_DOWN) {
            val keyCode = event.keyCode
            
            // Only handle the initial press (repeatCount 0)
            if (event.repeatCount == 0) {
                val now = System.currentTimeMillis()
                val settings = appSettings ?: settingsManager.getAppSettings()
                
                when (keyCode) {
                    KeyEvent.KEYCODE_DPAD_LEFT -> {
                        if (now - lastSeekTime > SEEK_DEBOUNCE_MS) {
                            lastSeekTime = now
                            player?.let {
                                val seekAmount = settings.seekBackwardSeconds * 1000L
                                val newPos = (it.currentPosition - seekAmount).coerceAtLeast(0)
                                Log.d(TAG, "Seek Back: ${it.currentPosition} -> $newPos (-${seekAmount}ms)")
                                it.seekTo(newPos)
                            }
                            return true 
                        }
                    }
                    KeyEvent.KEYCODE_DPAD_RIGHT -> {
                        if (now - lastSeekTime > SEEK_DEBOUNCE_MS) {
                            lastSeekTime = now
                            player?.let {
                                val seekAmount = settings.seekForwardSeconds * 1000L
                                val newPos = (it.currentPosition + seekAmount).coerceAtMost(it.duration)
                                Log.d(TAG, "Seek Forward: ${it.currentPosition} -> $newPos (+${seekAmount}ms)")
                                it.seekTo(newPos)
                            }
                            return true
                        }
                    }
                    KeyEvent.KEYCODE_DPAD_UP -> {
                        overlayManager.showOverlay()
                        return true
                    }
                    KeyEvent.KEYCODE_DPAD_DOWN -> {
                        overlayManager.hideOverlay()
                        return true
                    }
                    KeyEvent.KEYCODE_DPAD_CENTER, KeyEvent.KEYCODE_ENTER -> {
                        player?.let {
                            if (it.isPlaying) it.pause() else it.play()
                        }
                        return true
                    }
                    KeyEvent.KEYCODE_BACK, KeyEvent.KEYCODE_HOME -> {
                        saveCurrentPosition()
                        finish()
                        return true
                    }
                }
            } else if (keyCode == KeyEvent.KEYCODE_DPAD_LEFT || keyCode == KeyEvent.KEYCODE_DPAD_RIGHT) {
                // Consume repeat events
                return true
            }
        }
        return super.dispatchKeyEvent(event)
    }

    override fun onPause() {
        super.onPause()
        player?.pause()
    }

    override fun onDestroy() {
        super.onDestroy()
        // Clear keep screen on flag to restore screen saver behavior
        window.clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
        
        positionSaveHandler.removeCallbacks(positionSaveRunnable)
        saveCurrentPosition()
        player?.release()
        player = null
    }
}
