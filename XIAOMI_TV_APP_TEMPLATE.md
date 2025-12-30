# Xiaomi TV App Specification Template

## 1. Device Context
- **Target OS**: MiTV OS 2.9.6+ (Android 9.0 - 11.0+)
- **Architecture**: ARM64 (Cortex A55 focus)
- **Input**: Remote Control (D-pad navigation focus)

## 2. Technical Requirements (Best Practices)

### 2.1 Project Configuration
- **Min SDK**: 28 (Android 9.0)
- **Target SDK**: 34 (Android 14)
- **Gradle Version**: 8.2+
- **JDK Version**: 21 (Recommended)

### 2.2 Manifest Configuration
To ensure maximum visibility and functionality on Xiaomi TV:
```xml
<!-- 1. Include BOTH launcher categories -->
<intent-filter>
    <action android:name="android.intent.action.MAIN" />
    <category android:name="android.intent.category.LAUNCHER" />
    <category android:name="android.intent.category.LEANBACK_LAUNCHER" />
</intent-filter>

<!-- 2. Set Hardware requirements -->
<uses-feature android:name="android.hardware.touchscreen" android:required="false" />
<uses-feature android:name="android.software.leanback" android:required="true" />

<!-- 3. Permission for storage (Android 11+) -->
<uses-permission android:name="android.permission.MANAGE_EXTERNAL_STORAGE" />
<application android:requestLegacyExternalStorage="true" ... />
```

### 2.3 UI & Theme
- **Theme**: Must inherit from `Theme.AppCompat` (e.g., `Theme.AppCompat.NoActionBar`) if using `AppCompatActivity`.
- **Fonts**: Title (48sp), Headers (36sp), Body (28sp).
- **Navigation**: All interactive elements must have clear focus states for D-pad navigation.

## 3. Mandatory Solutions (Resolved Issues)

### 3.1 Exit Immediately Fix
Avoid `IllegalStateException` by ensuring the Activity theme matches the class type (AppCompat vs Leanback). Use `try-catch` in `onCreate` with logging.

### 3.2 Storage Access (Android 11+)
For apps requiring file write access outside of app-private folders:
1. Check `Environment.isExternalStorageManager()`.
2. If false, launch `Settings.ACTION_MANAGE_APP_ALL_FILES_ACCESS_PERMISSION`.
3. Inform the user they must find the app in the system list and toggle "Allow".

### 3.3 Path Visibility
Always use `Environment.getExternalStorageDirectory()` to target `/storage/emulated/0/` (Internal Storage) so files are visible to the user in the TV's built-in File Manager.

## 4. Lifecycle Management
- **Remote Exit**: Specifically handle `KEYCODE_BACK` and `KEYCODE_HOME` to ensure background services (like FTP) are stopped cleanly before the activity finishes.
- **Foreground Services**: Always use `foregroundServiceType` in the manifest and show a persistent notification to prevent the system from killing the app during file operations.

---
*Use this template as a base for all new Xiaomi TV development to avoid common pitfalls.*
