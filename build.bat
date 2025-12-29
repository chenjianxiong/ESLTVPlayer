@echo off
echo XiaoMi TV Video Player - Build Script
echo =====================================
echo.

REM Check for Android SDK
if not defined ANDROID_HOME (
    if exist "%LOCALAPPDATA%\Android\Sdk" (
        set ANDROID_HOME=%LOCALAPPDATA%\Android\Sdk
        echo Android SDK found: %ANDROID_HOME%
    ) else (
        echo ERROR: Android SDK not found!
        echo Please set ANDROID_HOME environment variable
        pause
        exit /b 1
    )
)

echo.
echo Step 1: Downloading Gradle Wrapper...
echo =====================================

REM Create gradlew if it doesn't exist
if not exist gradlew.bat (
    echo Gradle wrapper not found. Please run this from Android Studio first.
    echo Or download from: https://gradle.org/install/
    pause
    exit /b 1
)

echo.
echo Step 2: Syncing Gradle dependencies...
echo =====================================
call gradlew clean

echo.
echo Step 3: Building APK...
echo =====================================
call gradlew assembleDebug

echo.
echo =====================================
if exist app\build\outputs\apk\debug\app-debug.apk (
    echo BUILD SUCCESSFUL!
    echo.
    echo APK Location: app\build\outputs\apk\debug\app-debug.apk
    echo.
    echo To install on XiaoMi TV:
    echo   1. Enable USB Debugging on your TV
    echo   2. Connect via USB
    echo   3. Run: adb install app\build\outputs\apk\debug\app-debug.apk
) else (
    echo BUILD FAILED - Check error messages above
)

pause
