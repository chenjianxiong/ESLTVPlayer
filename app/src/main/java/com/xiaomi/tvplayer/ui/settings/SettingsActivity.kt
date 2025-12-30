package com.xiaomi.tvplayer.ui.settings

import android.graphics.Color
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.KeyEvent
import android.widget.Button
import android.widget.EditText
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
    private lateinit var btnDecreasePosX: Button
    private lateinit var btnIncreasePosX: Button
    private lateinit var tvPosXValue: TextView
    private lateinit var btnDecreasePosY: Button
    private lateinit var btnIncreasePosY: Button
    private lateinit var tvPosYValue: TextView
    private lateinit var btnDecreaseWidth: Button
    private lateinit var btnIncreaseWidth: Button
    private lateinit var tvWidthValue: TextView
    private lateinit var btnDecreaseHeight: Button
    private lateinit var btnIncreaseHeight: Button
    private lateinit var tvHeightValue: TextView
    private lateinit var btnCycleColor: Button
    private lateinit var btnDecreaseOpacity: Button
    private lateinit var btnIncreaseOpacity: Button
    private lateinit var tvOpacityValue: TextView

    // Directory Filter
    private lateinit var etDirectoryFilter: EditText

    // Display settings
    private lateinit var switchShowFileSize: SwitchCompat
    private lateinit var switchShowDuration: SwitchCompat

    private var seekBackwardSeconds = 5
    private var seekForwardSeconds = 5
    private var overlayConfig = OverlayConfig()
    
    private val colorList = listOf(
        Color.BLACK to "Black",
        Color.RED to "Red",
        Color.GREEN to "Green",
        Color.BLUE to "Blue",
        Color.WHITE to "White"
    )
    private var currentColorIndex = 0

    // TV Screen Reference (4K)
    private val TV_WIDTH = 3840f
    private val TV_HEIGHT = 2160f

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
        btnDecreasePosX = findViewById(R.id.btnDecreasePosX)
        btnIncreasePosX = findViewById(R.id.btnIncreasePosX)
        tvPosXValue = findViewById(R.id.tvPosXValue)
        btnDecreasePosY = findViewById(R.id.btnDecreasePosY)
        btnIncreasePosY = findViewById(R.id.btnIncreasePosY)
        tvPosYValue = findViewById(R.id.tvPosYValue)
        
        btnDecreaseWidth = findViewById(R.id.btnDecreaseWidth)
        btnIncreaseWidth = findViewById(R.id.btnIncreaseWidth)
        tvWidthValue = findViewById(R.id.tvWidthValue)
        btnDecreaseHeight = findViewById(R.id.btnDecreaseHeight)
        btnIncreaseHeight = findViewById(R.id.btnIncreaseHeight)
        tvHeightValue = findViewById(R.id.tvHeightValue)
        
        btnCycleColor = findViewById(R.id.btnCycleColor)
        
        btnDecreaseOpacity = findViewById(R.id.btnDecreaseOpacity)
        btnIncreaseOpacity = findViewById(R.id.btnIncreaseOpacity)
        tvOpacityValue = findViewById(R.id.tvOpacityValue)

        // Directory Filter
        etDirectoryFilter = findViewById(R.id.etDirectoryFilter)

        // Display settings
        switchShowFileSize = findViewById(R.id.switchShowFileSize)
        switchShowDuration = findViewById(R.id.switchShowDuration)
    }

    private fun loadSettings() {
        val settings = settingsManager.getAppSettings()

        seekBackwardSeconds = settings.seekBackwardSeconds
        seekForwardSeconds = settings.seekForwardSeconds
        updateSeekValues()

        overlayConfig = settings.overlayConfig
        switchOverlayEnabled.isChecked = overlayConfig.enabled
        tvPosXValue.text = overlayConfig.positionX.toString()
        tvPosYValue.text = overlayConfig.positionY.toString()
        tvWidthValue.text = overlayConfig.width.toString()
        tvHeightValue.text = overlayConfig.height.toString()
        tvOpacityValue.text = "${overlayConfig.opacity}%"
        
        currentColorIndex = colorList.indexOfFirst { it.first == overlayConfig.color }.coerceAtLeast(0)
        btnCycleColor.text = colorList[currentColorIndex].second

        etDirectoryFilter.setText(settings.directoryFilter)

        switchShowFileSize.isChecked = settings.showFileSize
        switchShowDuration.isChecked = settings.showDuration
    }

    private fun setupListeners() {
        btnDecreaseSeekBack.setOnClickListener { if (seekBackwardSeconds > 1) { seekBackwardSeconds--; updateSeekValues() } }
        btnIncreaseSeekBack.setOnClickListener { if (seekBackwardSeconds < 60) { seekBackwardSeconds++; updateSeekValues() } }
        btnDecreaseSeekForward.setOnClickListener { if (seekForwardSeconds > 1) { seekForwardSeconds--; updateSeekValues() } }
        btnIncreaseSeekForward.setOnClickListener { if (seekForwardSeconds < 60) { seekForwardSeconds++; updateSeekValues() } }
        
        // Position X
        btnDecreasePosX.setOnClickListener {
            overlayConfig = overlayConfig.copy(positionX = (overlayConfig.positionX - 10).coerceAtLeast(0))
            tvPosXValue.text = overlayConfig.positionX.toString()
        }
        btnIncreasePosX.setOnClickListener {
            overlayConfig = overlayConfig.copy(positionX = (overlayConfig.positionX + 10).coerceAtMost(TV_WIDTH.toInt()))
            tvPosXValue.text = overlayConfig.positionX.toString()
        }

        // Position Y
        btnDecreasePosY.setOnClickListener {
            overlayConfig = overlayConfig.copy(positionY = (overlayConfig.positionY - 10).coerceAtLeast(0))
            tvPosYValue.text = overlayConfig.positionY.toString()
        }
        btnIncreasePosY.setOnClickListener {
            overlayConfig = overlayConfig.copy(positionY = (overlayConfig.positionY + 10).coerceAtMost(TV_HEIGHT.toInt()))
            tvPosYValue.text = overlayConfig.positionY.toString()
        }

        // Width
        btnDecreaseWidth.setOnClickListener {
            overlayConfig = overlayConfig.copy(width = (overlayConfig.width - 10).coerceAtLeast(10))
            tvWidthValue.text = overlayConfig.width.toString()
        }
        btnIncreaseWidth.setOnClickListener {
            overlayConfig = overlayConfig.copy(width = (overlayConfig.width + 10).coerceAtMost(TV_WIDTH.toInt()))
            tvWidthValue.text = overlayConfig.width.toString()
        }

        // Height (adjusted to 5 pixels)
        btnDecreaseHeight.setOnClickListener {
            overlayConfig = overlayConfig.copy(height = (overlayConfig.height - 5).coerceAtLeast(5))
            tvHeightValue.text = overlayConfig.height.toString()
        }
        btnIncreaseHeight.setOnClickListener {
            overlayConfig = overlayConfig.copy(height = (overlayConfig.height + 5).coerceAtMost(TV_HEIGHT.toInt()))
            tvHeightValue.text = overlayConfig.height.toString()
        }

        // Color
        btnCycleColor.setOnClickListener {
            currentColorIndex = (currentColorIndex + 1) % colorList.size
            btnCycleColor.text = colorList[currentColorIndex].second
            overlayConfig = overlayConfig.copy(color = colorList[currentColorIndex].first)
        }
        
        // Opacity
        btnDecreaseOpacity.setOnClickListener {
            overlayConfig = overlayConfig.copy(opacity = (overlayConfig.opacity - 10).coerceAtLeast(0))
            tvOpacityValue.text = "${overlayConfig.opacity}%"
        }
        btnIncreaseOpacity.setOnClickListener {
            overlayConfig = overlayConfig.copy(opacity = (overlayConfig.opacity + 10).coerceAtMost(100))
            tvOpacityValue.text = "${overlayConfig.opacity}%"
        }
    }

    private fun updateSeekValues() {
        tvSeekBackValue.text = "${seekBackwardSeconds}s"
        tvSeekForwardValue.text = "${seekForwardSeconds}s"
    }

    private fun saveSettings() {
        val finalSettings = AppSettings(
            seekBackwardSeconds = seekBackwardSeconds,
            seekForwardSeconds = seekForwardSeconds,
            overlayConfig = overlayConfig.copy(enabled = switchOverlayEnabled.isChecked),
            directoryFilter = etDirectoryFilter.text.toString(),
            showFileSize = switchShowFileSize.isChecked,
            showDuration = switchShowDuration.isChecked
        )
        settingsManager.saveAppSettings(finalSettings)
        
        // Show "Settings saved" toast but cancel it quickly
        val toast = Toast.makeText(this, "Settings saved", Toast.LENGTH_SHORT)
        toast.show()
        Handler(Looper.getMainLooper()).postDelayed({
            toast.cancel()
        }, 800) // Hide after 800ms
    }

    override fun onKeyDown(keyCode: Int, event: KeyEvent?): Boolean {
        return when (keyCode) {
            KeyEvent.KEYCODE_BACK -> {
                saveSettings()
                finish()
                true
            }
            else -> super.onKeyDown(keyCode, event)
        }
    }
}
