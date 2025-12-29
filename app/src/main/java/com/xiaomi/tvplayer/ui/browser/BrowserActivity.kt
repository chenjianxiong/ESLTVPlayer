package com.xiaomi.tvplayer.ui.browser

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.view.KeyEvent
import android.view.View
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.xiaomi.tvplayer.R
import com.xiaomi.tvplayer.data.model.VideoFile
import com.xiaomi.tvplayer.manager.FileManager
import com.xiaomi.tvplayer.manager.SettingsManager
import com.xiaomi.tvplayer.ui.player.PlayerActivity
import com.xiaomi.tvplayer.ui.settings.SettingsActivity
import java.io.File

/**
 * Main file browser activity
 */
class BrowserActivity : AppCompatActivity() {

    private lateinit var rvFiles: RecyclerView
    private lateinit var tvCurrentPath: TextView
    private lateinit var tvEmptyState: TextView
    private lateinit var btnSettings: Button

    private lateinit var fileManager: FileManager
    private lateinit var settingsManager: SettingsManager
    private lateinit var fileAdapter: FileAdapter

    private var currentPath: String = "/sdcard/"
    private val pathStack = mutableListOf<String>()

    companion object {
        private const val REQUEST_STORAGE_PERMISSION = 1
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_browser)

        initializeViews()
        initializeManagers()
        checkPermissions()
    }

    private fun initializeViews() {
        rvFiles = findViewById(R.id.rvFiles)
        tvCurrentPath = findViewById(R.id.tvCurrentPath)
        tvEmptyState = findViewById(R.id.tvEmptyState)
        btnSettings = findViewById(R.id.btnSettings)

        rvFiles.layoutManager = LinearLayoutManager(this)

        btnSettings.setOnClickListener {
            openSettings()
        }
    }

    private fun initializeManagers() {
        fileManager = FileManager()
        settingsManager = SettingsManager(this)

        val settings = settingsManager.getAppSettings()

        // Initialize adapter
        fileAdapter = FileAdapter(
            files = emptyList(),
            showFileSize = settings.showFileSize,
            showDuration = settings.showDuration,
            onItemClick = { file -> onFileClick(file) }
        )
        rvFiles.adapter = fileAdapter

        // Restore last directory or use default
        currentPath = settingsManager.getLastDirectory()
    }

    private fun checkPermissions() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE)
            != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(
                this,
                arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE),
                REQUEST_STORAGE_PERMISSION
            )
        } else {
            loadFiles()
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == REQUEST_STORAGE_PERMISSION) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                loadFiles()
            } else {
                Toast.makeText(this, R.string.error_storage_permission, Toast.LENGTH_LONG).show()
            }
        }
    }

    private fun loadFiles() {
        val files = fileManager.getFilesInDirectory(currentPath)

        if (files.isEmpty()) {
            rvFiles.visibility = View.GONE
            tvEmptyState.visibility = View.VISIBLE
        } else {
            rvFiles.visibility = View.VISIBLE
            tvEmptyState.visibility = View.GONE
            fileAdapter.updateFiles(files)
        }

        tvCurrentPath.text = currentPath

        // Save last directory
        settingsManager.saveLastDirectory(currentPath)
    }

    private fun onFileClick(file: VideoFile) {
        if (file.isDirectory) {
            // Navigate to directory
            pathStack.add(currentPath)
            currentPath = file.path
            loadFiles()
        } else {
            // Play video
            openPlayer(file.path)
        }
    }

    private fun openPlayer(filePath: String) {
        val intent = Intent(this, PlayerActivity::class.java)
        intent.putExtra("FILE_PATH", filePath)
        startActivity(intent)
    }

    private fun openSettings() {
        val intent = Intent(this, SettingsActivity::class.java)
        startActivity(intent)
    }

    override fun onKeyDown(keyCode: Int, event: KeyEvent?): Boolean {
        return when (keyCode) {
            KeyEvent.KEYCODE_BACK -> {
                if (pathStack.isNotEmpty()) {
                    // Navigate up one directory
                    currentPath = pathStack.removeLast()
                    loadFiles()
                    true
                } else {
                    // Exit app
                    super.onKeyDown(keyCode, event)
                }
            }
            KeyEvent.KEYCODE_MENU -> {
                openSettings()
                true
            }
            else -> super.onKeyDown(keyCode, event)
        }
    }

    override fun onResume() {
        super.onResume()
        // Reload in case settings changed
        val settings = settingsManager.getAppSettings()
        fileAdapter = FileAdapter(
            files = emptyList(),
            showFileSize = settings.showFileSize,
            showDuration = settings.showDuration,
            onItemClick = { file -> onFileClick(file) }
        )
        rvFiles.adapter = fileAdapter
        loadFiles()
    }
}
