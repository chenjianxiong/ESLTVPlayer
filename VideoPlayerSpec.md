# XiaoMi TV Video Player - Technical Specification

## 1. Project Overview

### 1.1 Product Name
XiaoMi TV Video Player

### 1.2 Target Platform
- **Device**: XiaoMi TV (65-inch 4K optimized)
- **Architecture**: ARM64 (Cortex A55 focus)
- **Operating System**: MiTV OS 2.9.6+ (Android 9.0 - 14.0+)
- **Input**: Remote Control (D-pad navigation focus)

### 1.3 Purpose
A lightweight, feature-rich video player optimized for XiaoMi TV with custom navigation controls, overlay capabilities, and resume playback functionality.

---

## 2. System Requirements

### 2.1 Hardware Requirements
- **Minimum**: Cortex A55 4-core processor
- **RAM**: 1GB minimum
- **Storage**: 50MB for application installation
- **Display**: 720p minimum, 1080p/4K recommended (UI optimized for 1920x1080 logical canvas)
- **Touchscreen**: Not required (`android.hardware.touchscreen` set to false)

### 2.2 Software Requirements
- **OS**: MiTV OS 2.9.6 or higher
- **Android Version**: Android 9.0 (SDK 28) to Android 14.0 (SDK 34)
- **Min SDK**: 28
- **Target SDK**: 34
- **Development Environment**: Android Studio with Android TV SDK
- **Gradle Version**: 8.2+
- **JDK Version**: 21

### 2.3 Technology Stack
- **Language**: Kotlin
- **Media Framework**: ExoPlayer 2.18.1 (`StyledPlayerView`)
- **UI Framework**: Android TV Leanback Library / AppCompat
- **Build System**: Gradle with PowerShell automation (`build.ps1`)
- **Storage**: SharedPreferences for settings, SQLite (Room) for playback history

---

## 3. Feature Specifications

### 3.1 Video Format Support
- **Initial Support**: MP4 (H.264/H.265), MKV (Matroska)
- **Extended Support**: AVI, MOV, WebM, FLV
- **Codec Support**: H.264, H.265/HEVC, MPEG-4, AAC, MP3, AC3, DTS
- **Subtitles**: SRT, ASS, SSA

---

### 3.2 Navigation & Playback Controls

#### 3.2.1 Remote Control Mapping

| Button | Action | Implementation Detail |
|--------|--------|-----------------------|
| **Left** | Seek backward | Configurable (Default 5s), Debounced, Repeat-blocked |
| **Right** | Seek forward | Configurable (Default 5s), Debounced, Repeat-blocked |
| **Up** | Show overlay | Direct visibility toggle |
| **Down** | Hide overlay | Direct visibility toggle |
| **Center/OK** | Play/Pause | Toggle playback state |
| **Back/Return** | Exit / Go Back | Saves position, exits current screen |
| **Menu** | Open settings | Accessible from Browser and Player |

#### 3.2.2 Seek Functionality
- **Implementation**: Intercepted via `dispatchKeyEvent` to prevent double-seeks and hardware repeat issues.
- **UI Feedback**: Minimalist design; intrusive toast notifications removed for cleaner experience.
- **Power Management**: `FLAG_KEEP_SCREEN_ON` active during playback to disable screensaver.

---

### 3.3 Overlay Feature

#### 3.3.1 Overlay Properties (4K Default)
| Property | Default Value | Unit/Adjustment |
|----------|---------------|-----------------|
| **Color** | Black (#000000) | Cycle through presets |
| **Width** | 1400 | Pixels (10px increments) |
| **Height** | 55 | Pixels (5px increments) |
| **Position X** | 380 | Pixels (10px increments) |
| **Position Y** | 940 | Pixels (10px increments) |
| **Opacity** | 100% | Opaque (10% increments) |

#### 3.3.2 Overlay Behavior
- Persistent across seeks and pauses.
- Visibility controlled by Up/Down D-pad buttons.
- State saved automatically.

---

### 3.4 File Browser

#### 3.4.1 Storage Locations
- **Internal Storage**: Targeted via `Environment.getExternalStorageDirectory()`.
- **Navigation**: Support for hierarchical browsing with ".. [Go Back]" items and hardware Back button navigation.

#### 3.4.2 Storage Access (Android 11+)
- Mandatory `MANAGE_EXTERNAL_STORAGE` permission check.
- Automatic redirection to system settings for "All files access" authorization.

#### 3.4.3 Browser Features
- **Directory Filter**: Support for space-separated multiple search terms (e.g., "tftp download") to list specific folders.
- **Visual Feedback**: Focused items (Folders/Files/Settings Button) highlighted in **Blue** with a white border for high visibility on large screens.

---

### 3.5 Settings GUI

#### 3.5.1 Categories
- **Playback**: Precise seek timing adjustment (1-60s).
- **Overlay**: Full coordinate and dimension control with precise increments.
- **Directory Filter**: Text input for folder filtering.
- **Display**: Toggle file size and duration visibility.

#### 3.5.2 State Management
- **Auto-Save**: All settings are saved automatically when pressing the **Back** button.
- **Quick Confirmation**: 800ms transient toast confirm.
- **TV Optimized**: High-contrast blue focus states for all interactive elements.

---

## 4. Technical Architecture & Configuration

### 4.1 Build System (`build.ps1`)
Comprehensive PowerShell automation for Windows 10:
1. Clean Project
2. Build Debug (with strict error detection)
3. Build Release
4. Both
5. Install to TV (via ADB connection to 192.168.1.146)
6. Clear TV Logs
7. View TV Logs (Filtered for BrowserActivity)

### 4.2 Manifest Configuration
- **Launcher**: Dual registration for standard `LAUNCHER` and TV-specific `LEANBACK_LAUNCHER`.
- **Hardware**: `leanback` required, `touchscreen` false.
- **Permissions**: `MANAGE_EXTERNAL_STORAGE`, `WAKE_LOCK`.
- **Storage**: `android:requestLegacyExternalStorage="true"`.

### 4.3 Theme & Compatibility
- **Theme**: Inherits from `Theme.AppCompat.NoActionBar` to support `AppCompatActivity` while maintaining lean TV UI.
- **ExoPlayer**: Migrated to `StyledPlayerView` for modern component support.

---

## 5. Development Status

### Phase 1: Core Functionality & Xiaomi TV Optimization (COMPLETED)
- [x] Set up Android TV project (Min 28, Target 34).
- [x] Configure Manifest with dual launchers and hardware features.
- [x] Implement storage permission logic for Android 11+ (Manage External Storage).
- [x] Implement file browser with hierarchical navigation and Blue focus highlights.
- [x] Integrate modern ExoPlayer (`StyledPlayerView`).
- [x] Implement precision seek control and screensaver management.
- [x] Develop comprehensive Settings GUI with auto-save and 4K-optimized overlay defaults.
- [x] Implement space-separated directory filtering.
- [x] Create PowerShell build and deployment automation.

### Phase 2: Refinement (Next Steps)
- [ ] Multiple overlay support.
- [ ] Advanced subtitle settings.
- [ ] Library scanning background service.
- [ ] Network storage support (SMB/FTP).
