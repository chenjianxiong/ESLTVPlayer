# XiaoMi TV Video Player - Technical Specification

## 1. Project Overview

### 1.1 Product Name
XiaoMi TV Video Player

### 1.2 Target Platform
- **Device**: XiaoMi TV
- **Architecture**: ARM64 (Cortex A55 focus)
- **Operating System**: MiTV OS 2.9.6+ (Android 9.0 - 11.0+)
- **Input**: Remote Control (D-pad navigation focus)

### 1.3 Purpose
A lightweight, feature-rich video player optimized for XiaoMi TV with custom navigation controls, overlay capabilities, and resume playback functionality.

---

## 2. System Requirements

### 2.1 Hardware Requirements
- **Minimum**: Cortex A55 4-core processor
- **RAM**: 1GB minimum
- **Storage**: 50MB for application installation
- **Display**: 720p minimum, 1080p/4K recommended
- **Touchscreen**: Not required (`android.hardware.touchscreen` set to false)

### 2.2 Software Requirements
- **OS**: MiTV OS 2.9.6 or higher
- **Android Version**: Android 9.0 (SDK 28) to Android 14.0 (SDK 34)
- **Min SDK**: 28
- **Target SDK**: 34
- **Development Environment**: Android Studio with Android TV SDK
- **Gradle Version**: 8.2+
- **JDK Version**: 21 (Recommended)

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
- **Internal Storage**: Always use `Environment.getExternalStorageDirectory()` to target `/storage/emulated/0/` for user visibility.
- **External Storage**: USB drives mounted at `/mnt/`, `/storage/`
- **Automatic Detection**: Monitor for USB mount/unmount events

#### 3.4.2 Storage Access (Android 11+)
For full file access:
1. Include `MANAGE_EXTERNAL_STORAGE` permission in manifest.
2. Check `Environment.isExternalStorageManager()`.
3. If false, redirect user to `Settings.ACTION_MANAGE_APP_ALL_FILES_ACCESS_PERMISSION`.

#### 3.4.3 Browser Features
- Display folders and video files only (filter by extension)
- Navigate up/down through directory tree
- Show file metadata:
  - File name
  - File size
  - Duration (if available)
  - Last modified date
- Sort options: Name, Date, Size
- Remember last browsed directory

#### 3.4.4 Supported File Extensions Filter
```
.mp4, .mkv, .avi, .mov, .webm, .flv
```

#### 3.4.5 UI Layout
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
- **Home button press**: Handle `KEYCODE_HOME` specifically to ensure clean exit.
- **Back/Return button press**: Handle `KEYCODE_BACK`. From playback screen, exit to browser; from browser root, exit app.

#### 3.7.2 State Saving & Cleanup
On exit:
1. Save current playback position to database.
2. Save last browsed directory.
3. Save overlay state (visible/hidden).
4. **Clean Exit**: Stop any background services (e.g., file scanning) before finishing.
5. Release media player resources.
6. Clean up temporary files.

#### 3.7.3 Lifecycle Management
- Handle TV sleep/wake events.
- Manage audio focus.
- **Foreground Services**: Use foreground services with persistent notifications for long-running operations (like library scanning) to prevent system termination.

---

## 4. User Interface Design

### 4.1 Screen Structure
(See original spec for layout diagrams)

### 4.2 UI Guidelines
- Follow Android TV design guidelines.
- Use Leanback Library components.
- **Typography**: 
  - Title: 48sp
  - Headers: 36sp
  - Body: 28sp
- Ensure 10-foot UI readability.
- High contrast for text and icons.
- **Navigation**: All interactive elements must have clear focus states for D-pad navigation.
- Smooth animations (60fps target).

---

## 5. Technical Architecture & Configuration

### 5.1 Application Architecture
(See original spec for architecture diagram)

### 5.2 Manifest Configuration
To ensure maximum visibility and functionality on Xiaomi TV:
- **Launcher Categories**: Include both `android.intent.category.LAUNCHER` and `android.intent.category.LEANBACK_LAUNCHER`.
- **Hardware Requirements**: 
  - `android.hardware.touchscreen` required="false"
  - `android.software.leanback` required="true"
- **Permissions**: `MANAGE_EXTERNAL_STORAGE` for Android 11+.
- **Legacy Storage**: `android:requestLegacyExternalStorage="true"`.

### 5.3 Theme & Compatibility
- **Theme**: Must inherit from `Theme.AppCompat` (e.g., `Theme.AppCompat.NoActionBar`) to avoid `IllegalStateException` when using `AppCompatActivity`.
- **Error Handling**: Use `try-catch` in `onCreate` to catch initialization errors common on TV hardware.

### 5.4 Key Components
(See original spec for details on Media Player, File Manager, etc.)

---

## 6. Data Models
(See original spec for data model definitions)

---

## 7. Development Phases

### Phase 1: Core Functionality & Configuration (Week 1-2)
- [ ] Set up Android TV project with correct SDKs (Min 28, Target 34).
- [ ] Configure Manifest with dual launchers and hardware features.
- [ ] Implement storage permission logic for Android 11+.
- [ ] Implement basic file browser using `Environment.getExternalStorageDirectory()`.
- [ ] Integrate ExoPlayer.

(Subsequent phases follow original spec...)
