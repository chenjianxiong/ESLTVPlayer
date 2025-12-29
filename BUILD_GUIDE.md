# XiaoMi TV Video Player - Build & Deployment Guide

## Prerequisites

Before building the project, ensure you have:

1. **Android Studio** (Hedgehog 2023.1.1 or newer recommended)
   - Download from: https://developer.android.com/studio

2. **Android SDK**
   - Ensure **Android 14 (API 34)** is installed via SDK Manager.
   - Location: `C:\Users\<username>\AppData\Local\Android\Sdk`

3. **Java Development Kit (JDK)**
   - **JDK 17** is required for this project.
   - Download from: https://adoptium.net/

## Building the Project

### Option 1: Using the PowerShell Script (Simplest)

We provide a convenient PowerShell script that handles cleaning and building both Debug and Release versions.

1. **Run the build script:**
   ```powershell
   .\build.ps1
   ```
2. **Follow the on-screen prompts** to choose between Debug, Release, or both.

### Option 2: Using Android Studio

1. **Open Project**
   - `File` → `Open` → Select the `ESLTVPlayer` folder.
2. **Sync Gradle**
   - Android Studio should sync automatically. If not, click the "Sync Project with Gradle Files" icon.
3. **Build APK**
   - `Build` → `Build Bundle(s) / APK(s)` → `Build APK(s)`.
4. **Find APK**
   - Locate the APK at `app/build/outputs/apk/debug/esl-tvplayer-debug.apk`.

### Option 3: Manual Command Line (Gradlew)

```powershell
# Clean project
.\gradlew clean

# Build debug APK
.\gradlew :app:assembleDebug

# Build release APK
.\gradlew :app:assembleRelease
```

## Installing on XiaoMi TV

### Step 1: Enable Developer Options
1. Go to **Settings** → **About**.
2. Click **Build Number** 7 times rapidly until "You are now a developer!" appears.

### Step 2: Enable USB Debugging
1. Go to **Settings** → **Developer Options**.
2. Enable **USB Debugging** and **Install via USB**.

### Step 3: Connect and Install
**Wireless Connection (Recommended for TV):**
```powershell
# Connect to TV IP (found in Settings -> Network)
adb connect <TV_IP_ADDRESS>:5555

# Install the APK
# Note: Use the Debug APK for installation unless you have signed the Release APK.
adb install -r app\build\outputs\apk\debug\esl-tvplayer-debug.apk
```

## Troubleshooting

### Build Failures
1. **API Level 34**: This project requires `compileSdk 34`. Ensure this platform is installed in your SDK Manager.
2. **JDK Version**: Ensure `java -version` returns 17.
3. **Gradle 9.0 Compatibility**: The project has been updated to use `tasks.register` and `layout.buildDirectory` to avoid deprecation warnings.

### Installation Failures
1. **Release APK Unsigned**: If you build a Release APK, it will fail to install (`INSTALL_PARSE_FAILED_NO_CERTIFICATES`) because it is not signed. 
   - **Solution**: Use the **Debug** APK for development/testing.
   - **Advanced**: To sign a Release APK, you must create a keystore and configure `signingConfigs` in `app/build.gradle`.

### Common Room/Kapt Errors
If you see errors related to `PlaybackDao` or `Kapt`, perform a full clean:
```powershell
.\gradlew clean :app:assembleDebug
```

## Project Structure
- **app/**: Main application module.
- **build.ps1**: Interactive build script for PowerShell.
- **build.gradle**: Project-level configuration (Kotlin 1.9.22, AGP 8.2.2).
- **VideoPlayerSpec.md**: Detailed technical specification.

---
*Updated: December 2025*
