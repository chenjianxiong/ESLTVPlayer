package com.xiaomi.tvplayer.ui.settings

import android.os.Bundle
import android.view.KeyEvent
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.SwitchCompat
import com.xiaomi.tvplayer.R
import com.xiaomi.tvplayer.data.model.AppSettings
import com.xiaomi.tvplayer.data.model.OverlayConfig
import com.xiaomi.tvplayer.manager.SettingsManager

/**
 * Settings activity for configuring app preferences
 */
class SettingsActivity : AppCompatActivity() {

    private lateinit var settingsManager: SettingsManager

    // Playback settings
    private lateinit var btnDecreaseSeekBack: Button
    private lateinit var btnIncreaseSeekBack: Button
    private lateinit var tvSeekBackValue: TextView
    private lateinit var btnDecreaseSeekForward: Button
    private lateinit var btnIncreaseSeekForward: Button
    private lateinit var tvSeekForwardValue: TextView

    // Overlay settings
    private lateinit var switchOverlayEnabled: SwitchCompat
    private lateinit var tvOverlayWidth: TextView
    private lateinit var tvOverlayHeight: TextView
    private lateinit var tvOverlayOpacity: TextView

    // Display settings
    private lateinit var switchShowFileSize: SwitchCompat
    private lateinit var switchShowDuration: SwitchCompat

    private lateinit var btnSaveSettings: Button

    private var seekBackwardSeconds = 5
    private var seekForwardSeconds = 5
    private var overlayConfig = OverlayConfig()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_settings)

        settingsManager = SettingsManager(this)

        initializeViews()
        loadSettings()
        setupListeners()
    }

    private fun initializeViews() {
        // Playback settings
        btnDecreaseSeekBack = findViewById(R.id.btnDecreaseSeekBack)
        btnIncreaseSeekBack = findViewById(R.id.btnIncreaseSeekBack)
        tvSeekBackValue = findViewById(R.id.tvSeekBackValue)
        btnDecreaseSeekForward = findViewById(R.id.btnDecreaseSeekForward)
        btnIncreaseSeekForward = findViewById(R.id.btnIncreaseSeekForward)
        tvSeekForwardValue = findViewById(R.id.tvSeekForwardValue)

        // Overlay settings
        switchOverlayEnabled = findViewById(R.id.switchOverlayEnabled)
        tvOverlayWidth = findViewById(R.id.tvOverlayWidth)
        tvOverlayHeight = findViewById(R.id.tvOverlayHeight)
        tvOverlayOpacity = findViewById(R.id.tvOverlayOpacity)

        // Display settings
        switchShowFileSize = findViewById(R.id.switchShowFileSize)
        switchShowDuration = findViewById(R.id.switchShowDuration)

        btnSaveSettings = findViewById(R.id.btnSaveSettings)
    }

    private fun loadSettings() {
        val settings = settingsManager.getAppSettings()

        // Load playback settings
        seekBackwardSeconds = settings.seekBackwardSeconds
        seekForwardSeconds = settings.seekForwardSeconds
        updateSeekValues()

        // Load overlay settings
        overlayConfig = settings.overlayConfig
        switchOverlayEnabled.isChecked = overlayConfig.enabled
        tvOverlayWidth.text = "${overlayConfig.width}px"
        tvOverlayHeight.text = "${overlayConfig.height}px"
        tvOverlayOpacity.text = "${overlayConfig.opacity}%"

        // Load display settings
        switchShowFileSize.isChecked = settings.showFileSize
        switchShowDuration.isChecked = settings.showDuration
    }

    private fun setupListeners() {
        // Seek backward buttons
        btnDecreaseSeekBack.setOnClickListener {
            if (seekBackwardSeconds > 1) {
                seekBackwardSeconds--
                updateSeekValues()
            }
        }

        btnIncreaseSeekBack.setOnClickListener {
            if (seekBackwardSeconds < 60) {
                seekBackwardSeconds++
                updateSeekValues()
            }
        }

        // Seek forward buttons
        btnDecreaseSeekForward.setOnClickListener {
            if (seekForwardSeconds > 1) {
                seekForwardSeconds--
                updateSeekValues()
            }
        }

        btnIncreaseSeekForward.setOnClickListener {
            if (seekForwardSeconds < 60) {
                seekForwardSeconds++
                updateSeekValues()
            }
        }

        // Save button
        btnSaveSettings.setOnClickListener {
            saveSettings()
        }
    }

    private fun updateSeekValues() {
        tvSeekBackValue.text = "${seekBackwardSeconds}s"
        tvSeekForwardValue.text = "${seekForwardSeconds}s"
    }

    private fun saveSettings() {
        val newOverlayConfig = overlayConfig.copy(
            enabled = switchOverlayEnabled.isChecked
        )

        val newSettings = AppSettings(
            seekBackwardSeconds = seekBackwardSeconds,
            seekForwardSeconds = seekForwardSeconds,
            overlayConfig = newOverlayConfig,
            showFileSize = switchShowFileSize.isChecked,
            showDuration = switchShowDuration.isChecked
        )

        settingsManager.saveAppSettings(newSettings)

        Toast.makeText(this, "Settings saved", Toast.LENGTH_SHORT).show()
        finish()
    }

    override fun onKeyDown(keyCode: Int, event: KeyEvent?): Boolean {
        return when (keyCode) {
            KeyEvent.KEYCODE_BACK -> {
                finish()
                true
            }
            else -> super.onKeyDown(keyCode, event)
        }
    }
}
