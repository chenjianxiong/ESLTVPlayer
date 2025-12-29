# XiaoMi TV Video Player - Technical Specification

## 1. Project Overview

### 1.1 Product Name
XiaoMi TV Video Player

### 1.2 Target Platform
- **Device**: XiaoMi TV
- **Processor**: Cortex A55 (4 cores)
- **Operating System**: MiTV OS 2.9.6
- **Development Framework**: Android TV SDK (MiTV OS is based on Android)

### 1.3 Purpose
A lightweight, feature-rich video player optimized for XiaoMi TV with custom navigation controls, overlay capabilities, and resume playback functionality.

---

## 2. System Requirements

### 2.1 Hardware Requirements
- **Minimum**: Cortex A55 4-core processor
- **RAM**: 1GB minimum
- **Storage**: 50MB for application installation
- **Display**: 720p minimum, 1080p/4K recommended

### 2.2 Software Requirements
- **OS**: MiTV OS 2.9.6 or higher
- **Android Version**: Android TV 9.0+ (typical for MiTV OS 2.9.6)
- **Development Environment**: Android Studio with Android TV SDK

### 2.3 Technology Stack
- **Language**: Kotlin/Java
- **Media Framework**: ExoPlayer 2.x or MediaPlayer API
- **UI Framework**: Android TV Leanback Library
- **Build System**: Gradle
- **Storage**: SharedPreferences for settings, SQLite for playback history

---

## 3. Feature Specifications

### 3.1 Video Format Support

#### 3.1.1 Initial Support
- **MP4** (H.264/H.265 codec)
- **MKV** (Matroska container)

#### 3.1.2 Future Extensions
- AVI
- MOV
- WebM
- FLV

#### 3.1.3 Codec Support
- Video: H.264, H.265/HEVC, MPEG-4
- Audio: AAC, MP3, AC3, DTS
- Subtitles: SRT, ASS, SSA (embedded and external)

---

### 3.2 Navigation & Playback Controls

#### 3.2.1 Remote Control Mapping

| Button | Action | Default Value | Configurable |
|--------|--------|---------------|--------------|
| **Left** | Seek backward | 5 seconds | Yes |
| **Right** | Seek forward | 5 seconds | Yes |
| **Up** | Show overlay | N/A | No |
| **Down** | Hide overlay | N/A | No |
| **Center/OK** | Play/Pause | N/A | No |
| **Home** | Exit player (save position) | N/A | No |
| **Back/Return** | Exit player (save position) | N/A | No |
| **Menu** | Open settings | N/A | No |

#### 3.2.2 Seek Functionality
- **Default Values**: 5 seconds backward/forward
- **Configurable Range**: 1-60 seconds
- **Implementation**: Frame-accurate seeking when possible
- **UI Feedback**: Toast or on-screen display showing seek amount

---

### 3.3 Overlay Feature

#### 3.3.1 Overlay Properties
All properties configurable via Settings GUI:

| Property | Default Value | Range/Options |
|----------|---------------|---------------|
| **Color** | Red (#FF0000) | RGB color picker |
| **Width** | 200px | 50-800px |
| **Height** | 100px | 50-600px |
| **Position X** | Center | 0 - screen width |
| **Position Y** | Center | 0 - screen height |
| **Opacity** | 80% | 0-100% |

#### 3.3.2 Overlay Behavior
- Toggle visibility with Up (show) and Down (hide) buttons
- Remains visible across seeks and pauses
- Saved state: Hidden by default on playback start
- Optional: Allow multiple overlays (future enhancement)

#### 3.3.3 Use Cases
- Testing display regions
- Marking areas of interest
- Developer/debugging purposes

---

### 3.4 File Browser

#### 3.4.1 Storage Locations
- **Internal Storage**: `/sdcard/`, `/storage/emulated/0/`
- **External Storage**: USB drives mounted at `/mnt/`, `/storage/`
- **Automatic Detection**: Monitor for USB mount/unmount events

#### 3.4.2 Browser Features
- Display folders and video files only (filter by extension)
- Navigate up/down through directory tree
- Show file metadata:
  - File name
  - File size
  - Duration (if available)
  - Last modified date
- Sort options: Name, Date, Size
- Remember last browsed directory

#### 3.4.3 Supported File Extensions Filter
```
.mp4, .mkv, .avi, .mov, .webm, .flv
```

#### 3.4.4 UI Layout
- Grid or List view (user preference)
- Folder icon vs. video icon differentiation
- Breadcrumb navigation showing current path
- Fast scrolling with letter jumper (A-Z)

---

### 3.5 Resume Playback

#### 3.5.1 Position Tracking
- Save playback position every 5 seconds
- Save on exit (Home/Back button)
- Store in local database (SQLite)

#### 3.5.2 Database Schema
```sql
CREATE TABLE playback_history (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    file_path TEXT UNIQUE NOT NULL,
    position_ms INTEGER NOT NULL,
    duration_ms INTEGER,
    last_played TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);
```

#### 3.5.3 Resume Behavior
- On file selection, check database for previous position
- If position exists and > 5 seconds, show resume dialog:
  - "Resume from [MM:SS]" (default selected)
  - "Play from beginning"
- Auto-select resume after 10 seconds
- Clear position after video ends (>=95% watched)

---

### 3.6 Settings GUI

#### 3.6.1 Settings Categories

**A. Playback Settings**
- Seek backward time (1-60 seconds)
- Seek forward time (1-60 seconds)
- Auto-play next video (future)
- Subtitle settings (future)

**B. Overlay Settings**
- Enable/Disable overlay feature
- Color picker (RGB/HEX)
- Width (50-800px)
- Height (50-600px)
- Position X (0-screen width)
- Position Y (0-screen height)
- Opacity (0-100%)
- Preview overlay

**C. Display Settings**
- File browser view mode (Grid/List)
- Show file size
- Show duration
- Sort preference

**D. Storage Settings**
- Default starting directory
- Scan external storage on launch
- Refresh storage locations

**E. About**
- App version
- Supported formats
- License information

#### 3.6.2 Settings Access
- Press Menu button during playback or in browser
- Dedicated settings icon in main browser screen

#### 3.6.3 Settings Storage
- Use SharedPreferences for all settings
- JSON format for complex settings (overlay config)
- Export/Import settings (future enhancement)

---

### 3.7 Exit & State Management

#### 3.7.1 Exit Triggers
- Home button press
- Back/Return button press (from playback screen)
- Multiple Back presses (from browser screen)

#### 3.7.2 State Saving
On exit:
1. Save current playback position to database
2. Save last browsed directory
3. Save overlay state (visible/hidden)
4. Release media player resources
5. Clean up temporary files

#### 3.7.3 Lifecycle Management
- Handle TV sleep/wake
- Manage audio focus
- Handle incoming calls/notifications (if applicable)

---

## 4. User Interface Design

### 4.1 Screen Structure

#### 4.1.1 Main Screen (File Browser)
```
┌─────────────────────────────────────────────┐
│  XiaoMi Video Player         [Settings]     │
├─────────────────────────────────────────────┤
│  📁 Internal Storage > Movies               │
├─────────────────────────────────────────────┤
│  📁 Folder1                                  │
│  📁 Folder2                                  │
│  🎬 movie1.mp4          1.2GB  02:15:30     │
│  🎬 movie2.mkv          850MB  01:45:20     │
│  🎬 movie3.mp4          2.1GB  02:45:10     │
│  [USB Drive: /mnt/usb1]                     │
└─────────────────────────────────────────────┘
```

#### 4.1.2 Playback Screen
```
┌─────────────────────────────────────────────┐
│                                              │
│           [Video Content Area]              │
│                                              │
│              [Overlay - if visible]         │
│                                              │
│                                              │
├─────────────────────────────────────────────┤
│  ▶ Movie Title          01:23:45 / 02:15:30 │
│  ━━━━━━━━━━━━━━━━━●━━━━━━━━━━━━━━━━━━━━━  │
└─────────────────────────────────────────────┘
```

#### 4.1.3 Settings Screen
```
┌─────────────────────────────────────────────┐
│  Settings                                    │
├─────────────────────────────────────────────┤
│  ▸ Playback Settings                        │
│  ▸ Overlay Settings                         │
│  ▸ Display Settings                         │
│  ▸ Storage Settings                         │
│  ▸ About                                    │
└─────────────────────────────────────────────┘
```

### 4.2 UI Guidelines
- Follow Android TV design guidelines
- Use Leanback Library components
- Ensure 10-foot UI readability
- High contrast for text and icons
- Smooth animations (60fps target)
- Clear focus indicators

---

## 5. Technical Architecture

### 5.1 Application Architecture

```
┌─────────────────────────────────────────────┐
│         Presentation Layer (UI)             │
│  - BrowserActivity                          │
│  - PlayerActivity                           │
│  - SettingsActivity                         │
└────────────────┬────────────────────────────┘
                 │
┌────────────────▼────────────────────────────┐
│         Business Logic Layer                │
│  - PlaybackManager                          │
│  - FileManager                              │
│  - SettingsManager                          │
│  - OverlayManager                           │
└────────────────┬────────────────────────────┘
                 │
┌────────────────▼────────────────────────────┐
│            Data Layer                       │
│  - PlaybackDatabase (SQLite)                │
│  - SharedPreferences                        │
│  - FileSystem Access                        │
└─────────────────────────────────────────────┘
```

### 5.2 Key Components

#### 5.2.1 Media Player
- **Primary**: ExoPlayer 2.x (recommended for format flexibility)
- **Fallback**: Android MediaPlayer API
- **Features**: Hardware acceleration, codec support, adaptive streaming

#### 5.2.2 File Manager
- Scan storage locations
- Filter video files
- Provide file metadata
- Monitor USB mount events

#### 5.2.3 Playback Manager
- Control media playback
- Track position
- Handle seek operations
- Manage resume functionality

#### 5.2.4 Overlay Manager
- Render overlay view
- Apply settings (color, size, position)
- Toggle visibility

#### 5.2.5 Settings Manager
- Load/Save settings
- Validate input ranges
- Provide defaults

---

## 6. Data Models

### 6.1 PlaybackRecord
```kotlin
data class PlaybackRecord(
    val id: Long = 0,
    val filePath: String,
    val positionMs: Long,
    val durationMs: Long,
    val lastPlayed: Long
)
```

### 6.2 VideoFile
```kotlin
data class VideoFile(
    val path: String,
    val name: String,
    val size: Long,
    val duration: Long?,
    val lastModified: Long,
    val isDirectory: Boolean
)
```

### 6.3 OverlayConfig
```kotlin
data class OverlayConfig(
    val enabled: Boolean = true,
    val color: Int = Color.RED,
    val width: Int = 200,
    val height: Int = 100,
    val positionX: Int = -1, // -1 for center
    val positionY: Int = -1, // -1 for center
    val opacity: Int = 80
)
```

### 6.4 AppSettings
```kotlin
data class AppSettings(
    val seekBackwardSeconds: Int = 5,
    val seekForwardSeconds: Int = 5,
    val overlayConfig: OverlayConfig = OverlayConfig(),
    val browserViewMode: ViewMode = ViewMode.GRID,
    val defaultDirectory: String = "/sdcard/",
    val showFileSize: Boolean = true,
    val showDuration: Boolean = true
)
```

---

## 7. Development Phases

### Phase 1: Core Functionality (Week 1-2)
- [ ] Set up Android TV project
- [ ] Implement basic file browser
- [ ] Integrate ExoPlayer
- [ ] Basic playback controls (play/pause)
- [ ] MP4/MKV format support

### Phase 2: Navigation Controls (Week 2-3)
- [ ] Implement seek backward/forward
- [ ] Remote control button mapping
- [ ] On-screen feedback for seeks
- [ ] Default 5-second configuration

### Phase 3: Resume Playback (Week 3-4)
- [ ] SQLite database setup
- [ ] Position tracking implementation
- [ ] Resume dialog UI
- [ ] Position save on exit

### Phase 4: Overlay Feature (Week 4-5)
- [ ] Overlay view component
- [ ] Show/hide functionality
- [ ] Default overlay configuration
- [ ] Overlay rendering on video

### Phase 5: Settings GUI (Week 5-6)
- [ ] Settings activity layout
- [ ] Seek time configuration
- [ ] Overlay configuration UI
- [ ] Color picker implementation
- [ ] Settings persistence

### Phase 6: Storage Management (Week 6-7)
- [ ] Internal storage access
- [ ] USB drive detection
- [ ] Mount/unmount event handling
- [ ] Multi-source file browser

### Phase 7: Polish & Optimization (Week 7-8)
- [ ] UI refinement
- [ ] Performance optimization
- [ ] Memory management
- [ ] Error handling
- [ ] Testing on XiaoMi TV

### Phase 8: Testing & Bug Fixes (Week 8-9)
- [ ] Functional testing
- [ ] UI/UX testing
- [ ] Performance testing
- [ ] Bug fixes

---

## 8. Technical Considerations

### 8.1 Performance Optimization
- **Hardware Acceleration**: Enable hardware decoding for H.264/H.265
- **Memory Management**: Release resources when not in use
- **Thumbnail Caching**: Cache video thumbnails for faster browsing
- **Lazy Loading**: Load file list on-demand for large directories

### 8.2 Error Handling
- Unsupported format notification
- Corrupted file detection
- Storage permission errors
- USB drive disconnection during playback
- Network errors (for future streaming)

### 8.3 Security & Permissions
- READ_EXTERNAL_STORAGE
- WRITE_EXTERNAL_STORAGE (for position saving)
- INTERNET (future streaming)
- Wake lock for continuous playback

### 8.4 Testing Strategy
- **Unit Tests**: Business logic, data models
- **Integration Tests**: Database, file operations
- **UI Tests**: Navigation, playback controls
- **Device Testing**: Real XiaoMi TV testing

---

## 9. Future Enhancements

### 9.1 Short-term (v2.0)
- Subtitle support (SRT, ASS)
- Audio track selection
- Playback speed control
- Playlist management

### 9.2 Medium-term (v3.0)
- Network streaming (SMB, NFS, HTTP)
- Video thumbnails in browser
- Resume playback sync across devices
- Gesture controls

### 9.3 Long-term (v4.0)
- Cloud storage integration
- Video library management
- Metadata fetching (TMDB/IMDB)
- Advanced filters and search

---

## 10. Deployment

### 10.1 Build Configuration
- **Target SDK**: Android TV API level 28+
- **Min SDK**: Android TV API level 21
- **Package Name**: com.xiaomi.tvplayer
- **Version Code**: 1
- **Version Name**: 1.0.0

### 10.2 Installation
- APK sideloading via USB
- Future: XiaoMi App Store submission

### 10.3 Distribution
- GitHub releases
- Direct APK download
- Future: Official app store

---

## 11. Success Criteria

### 11.1 Functional Requirements
- ✓ Plays MP4 and MKV files smoothly
- ✓ Seek backward/forward works reliably
- ✓ Overlay displays and hides correctly
- ✓ File browser shows all accessible storage
- ✓ Resume playback works consistently
- ✓ Settings are saved and applied correctly
- ✓ Clean exit with position saving

### 11.2 Performance Requirements
- Video starts within 2 seconds
- Seek operations complete within 1 second
- UI remains responsive (60fps)
- Memory usage < 200MB during playback
- No crashes during 2-hour continuous playback

### 11.3 Usability Requirements
- Intuitive remote control navigation
- Clear visual feedback for all actions
- Settings are easy to understand and configure
- Minimal steps to start watching a video

---

## 12. Appendix

### 12.1 Remote Control Key Codes
```kotlin
// Android TV Key Codes
KEYCODE_DPAD_CENTER     // OK/Select
KEYCODE_DPAD_UP         // Up
KEYCODE_DPAD_DOWN       // Down
KEYCODE_DPAD_LEFT       // Left
KEYCODE_DPAD_RIGHT      // Right
KEYCODE_BACK            // Back/Return
KEYCODE_HOME            // Home
KEYCODE_MENU            // Menu
```

### 12.2 File Path Examples
```
Internal: /storage/emulated/0/Movies/
USB Drive: /mnt/usb_storage/
External SD: /storage/sdcard1/
```

### 12.3 Useful Libraries
- ExoPlayer: `com.google.android.exoplayer:exoplayer:2.18.1`
- Leanback: `androidx.leanback:leanback:1.0.0`
- Room: `androidx.room:room-runtime:2.5.0` (alternative to SQLite)
- Glide: `com.github.bumptech.glide:glide:4.15.1` (thumbnails)

### 12.4 References
- [Android TV Developer Guide](https://developer.android.com/training/tv)
- [ExoPlayer Documentation](https://exoplayer.dev/)
- [Leanback Library](https://developer.android.com/tv/develop/leanback)
- [MiTV OS Documentation](https://www.mi.com/global/mitv)

---

## Document Information
- **Version**: 1.0
- **Date**: December 29, 2025
- **Author**: Development Team
- **Status**: Draft for Review

---

**End of Specification Document**
