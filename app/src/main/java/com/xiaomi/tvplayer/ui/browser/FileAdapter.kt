package com.xiaomi.tvplayer.ui.browser

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.xiaomi.tvplayer.R
import com.xiaomi.tvplayer.data.model.VideoFile
import com.xiaomi.tvplayer.manager.FileManager

/**
 * Adapter for displaying video files and directories
 */
class FileAdapter(
    private var files: List<VideoFile>,
    private val showFileSize: Boolean,
    private val showDuration: Boolean,
    private val onItemClick: (VideoFile) -> Unit
) : RecyclerView.Adapter<FileAdapter.FileViewHolder>() {

    private val fileManager = FileManager()

    class FileViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val tvIcon: TextView = view.findViewById(R.id.tvIcon)
        val tvFileName: TextView = view.findViewById(R.id.tvFileName)
        val tvFileSize: TextView = view.findViewById(R.id.tvFileSize)
        val tvDuration: TextView = view.findViewById(R.id.tvDuration)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): FileViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_file, parent, false)
        return FileViewHolder(view)
    }

    override fun onBindViewHolder(holder: FileViewHolder, position: Int) {
        val file = files[position]

        // Set icon
        holder.tvIcon.text = if (file.isDirectory) "📁" else "🎬"

        // Set file name
        holder.tvFileName.text = file.name

        // Set file size
        if (showFileSize && !file.isDirectory) {
            holder.tvFileSize.text = fileManager.formatFileSize(file.size)
            holder.tvFileSize.visibility = View.VISIBLE
        } else {
            holder.tvFileSize.visibility = View.GONE
        }

        // Set duration
        if (showDuration && file.duration != null && !file.isDirectory) {
            holder.tvDuration.text = fileManager.formatDuration(file.duration)
            holder.tvDuration.visibility = View.VISIBLE
        } else {
            holder.tvDuration.visibility = View.GONE
        }

        // Click listener
        holder.itemView.setOnClickListener {
            onItemClick(file)
        }

        // Focus listener for TV navigation
        holder.itemView.setOnFocusChangeListener { _, hasFocus ->
            holder.itemView.isSelected = hasFocus
        }
    }

    override fun getItemCount(): Int = files.size

    fun updateFiles(newFiles: List<VideoFile>) {
        files = newFiles
        notifyDataSetChanged()
    }
}
