# XiaoMi TV Video Player - PowerShell Build Script
Write-Host "XiaoMi TV Video Player - Build Script" -ForegroundColor Cyan
Write-Host "====================================="
Write-Host ""

# 1. Environment Check
if (-not $env:ANDROID_HOME) {
    $localPath = "$env:LOCALAPPDATA\Android\Sdk"
    if (Test-Path $localPath) {
        $env:ANDROID_HOME = $localPath
        Write-Host "Found Android SDK at: $env:ANDROID_HOME" -ForegroundColor Gray
    } else {
        Write-Error "ANDROID_HOME is not set and could not be found in local app data."
        Write-Host "Please set the ANDROID_HOME environment variable to your Android SDK path." -ForegroundColor Red
        return
    }
}

# 2. Check for Gradle Wrapper
if (-not (Test-Path ".\gradlew.bat")) {
    Write-Error "gradlew.bat not found in the current directory."
    Write-Host "Ensure you are running this script from the project root." -ForegroundColor Red
    return
}

# 3. Clean Project
Write-Host "`nStep 1: Cleaning Project..." -ForegroundColor Yellow
Write-Host "==========================="
& .\gradlew.bat clean

if ($LASTEXITCODE -ne 0) {
    Write-Host "`nClean failed. Check logs above." -ForegroundColor Red
    return
}

# 4. Build Options
Write-Host "`nChoose build type:" -ForegroundColor Cyan
Write-Host "1. Debug (Default)"
Write-Host "2. Release"
Write-Host "3. Both"
$choice = Read-Host "Enter choice [1-3]"

if ($choice -eq "2") {
    $tasks = "assembleRelease"
} elseif ($choice -eq "3") {
    $tasks = "assembleDebug assembleRelease"
} else {
    $tasks = "assembleDebug"
}

# 5. Build
Write-Host "`nStep 2: Building $tasks..." -ForegroundColor Yellow
Write-Host "========================================"
& .\gradlew.bat $tasks

# 6. Result Check
Write-Host "`n============================="
$debugApk = "app\build\outputs\apk\debug\esl-tvplayer-debug.apk"
$releaseApk = "app\build\outputs\apk\release\esl-tvplayer-release.apk"

if (Test-Path $debugApk) {
    Write-Host "DEBUG APK: $debugApk" -ForegroundColor White
}
if (Test-Path $releaseApk) {
    Write-Host "RELEASE APK: $releaseApk" -ForegroundColor White
    Write-Host "Note: Release APK needs to be signed before installation." -ForegroundColor Yellow
}

if (-not (Test-Path $debugApk) -and -not (Test-Path $releaseApk)) {
    Write-Host "BUILD FAILED" -ForegroundColor Red
} else {
    Write-Host "`nBUILD SUCCESSFUL!" -ForegroundColor Green
}

Write-Host "`nPress any key to exit..."
$Host.UI.RawUI.ReadKey("NoEcho,IncludeKeyDown") | Out-Null
