package com.xiaomi.tvplayer.ui.browser

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.util.Log
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

    private val TAG = "BrowserActivity"

    private lateinit var rvFiles: RecyclerView
    private lateinit var tvCurrentPath: TextView
    private lateinit var tvEmptyState: TextView
    private lateinit var btnSettings: Button

    private lateinit var fileManager: FileManager
    private lateinit var settingsManager: SettingsManager
    private lateinit var fileAdapter: FileAdapter

    private var currentPath: String = "/sdcard/"

    companion object {
        private const val REQUEST_STORAGE_PERMISSION = 1
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        Log.d(TAG, "onCreate started")
        super.onCreate(savedInstanceState)
        try {
            setContentView(R.layout.activity_browser)

            initializeViews()
            initializeManagers()
            checkPermissions()
            Log.d(TAG, "onCreate finished successfully")
        } catch (e: Exception) {
            Log.e(TAG, "Error in onCreate: ${e.message}", e)
            Toast.makeText(this, "Error starting app: ${e.message}", Toast.LENGTH_LONG).show()
        }
    }

    private fun initializeViews() {
        Log.d(TAG, "Initializing views")
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
        Log.d(TAG, "Initial path set to: $currentPath")
    }

    private fun checkPermissions() {
        Log.d(TAG, "Checking permissions")
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE)
            != PackageManager.PERMISSION_GRANTED) {
            Log.d(TAG, "Requesting storage permissions")
            ActivityCompat.requestPermissions(
                this,
                arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.WRITE_EXTERNAL_STORAGE),
                REQUEST_STORAGE_PERMISSION
            )
        } else {
            Log.d(TAG, "Permissions already granted, loading files")
            loadFiles()
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        Log.d(TAG, "onRequestPermissionsResult: $requestCode")
        if (requestCode == REQUEST_STORAGE_PERMISSION) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Log.d(TAG, "Permission granted by user")
                loadFiles()
            } else {
                Log.w(TAG, "Permission denied by user")
                Toast.makeText(this, R.string.error_storage_permission, Toast.LENGTH_LONG).show()
            }
        }
    }

    private fun loadFiles() {
        Log.d(TAG, "Loading files from: $currentPath")
        try {
            val settings = settingsManager.getAppSettings()
            val filterString = settings.directoryFilter.trim()
            // Split by space instead of comma
            val filters = if (filterString.isNotEmpty()) {
                filterString.split("\\s+".toRegex()).map { it.lowercase() }.filter { it.isNotEmpty() }
            } else {
                emptyList()
            }

            val fileList = mutableListOf<VideoFile>()
            
            // Add "Back to Parent" option if not at root
            val currentFile = File(currentPath)
            val parentFile = currentFile.parentFile
            if (parentFile != null && currentPath != "/" && currentPath != "/storage/emulated/0" && currentPath != "/sdcard") {
                fileList.add(VideoFile(
                    path = parentFile.absolutePath,
                    name = ".. [Go Back]",
                    size = 0,
                    lastModified = 0,
                    isDirectory = true
                ))
            }

            val allFiles = fileManager.getFilesInDirectory(currentPath)
            
            // Apply filtering logic
            val filteredFiles = if (filters.isEmpty()) {
                allFiles
            } else {
                allFiles.filter { file ->
                    if (file.isDirectory) {
                        // Check if directory name matches any filter
                        filters.any { filter -> file.name.lowercase().contains(filter) }
                    } else {
                        // Always show video files
                        true
                    }
                }
            }
            
            fileList.addAll(filteredFiles)

            if (fileList.isEmpty()) {
                Log.d(TAG, "No files found matching filter")
                rvFiles.visibility = View.GONE
                tvEmptyState.visibility = View.VISIBLE
            } else {
                Log.d(TAG, "Found ${fileList.size} files")
                rvFiles.visibility = View.VISIBLE
                tvEmptyState.visibility = View.GONE
                fileAdapter.updateFiles(fileList)
            }

            tvCurrentPath.text = currentPath

            // Save last directory
            settingsManager.saveLastDirectory(currentPath)
        } catch (e: Exception) {
            Log.e(TAG, "Error loading files: ${e.message}", e)
        }
    }

    private fun onFileClick(file: VideoFile) {
        Log.d(TAG, "File clicked: ${file.name}")
        if (file.isDirectory) {
            currentPath = file.path
            loadFiles()
        } else {
            openPlayer(file.path)
        }
    }

    private fun openPlayer(filePath: String) {
        Log.d(TAG, "Opening player for: $filePath")
        val intent = Intent(this, PlayerActivity::class.java)
        intent.putExtra("FILE_PATH", filePath)
        startActivity(intent)
    }

    private fun openSettings() {
        Log.d(TAG, "Opening settings")
        val intent = Intent(this, SettingsActivity::class.java)
        startActivity(intent)
    }

    override fun onKeyDown(keyCode: Int, event: KeyEvent?): Boolean {
        Log.d(TAG, "onKeyDown: $keyCode")
        return when (keyCode) {
            KeyEvent.KEYCODE_BACK -> {
                val currentFile = File(currentPath)
                val parentFile = currentFile.parentFile
                
                // If we can go up, go up. Otherwise, exit the app.
                if (parentFile != null && currentPath != "/" && currentPath != "/storage/emulated/0" && currentPath != "/sdcard") {
                    currentPath = parentFile.absolutePath
                    loadFiles()
                    true
                } else {
                    Log.d(TAG, "Exiting app via Back")
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

    override fun onStart() {
        super.onStart()
        Log.d(TAG, "onStart")
    }

    override fun onResume() {
        super.onResume()
        Log.d(TAG, "onResume")
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

    override fun onPause() {
        super.onPause()
        Log.d(TAG, "onPause")
    }

    override fun onStop() {
        super.onStop()
        Log.d(TAG, "onStop")
    }

    override fun onDestroy() {
        super.onDestroy()
        Log.d(TAG, "onDestroy")
    }
}
