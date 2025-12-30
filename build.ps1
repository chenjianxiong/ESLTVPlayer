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

$adb = "$env:ANDROID_HOME\platform-tools\adb.exe"
$tvIp = "192.168.1.146"
$packageName = "com.xiaomi.tvplayer"

# 2. Check for Gradle Wrapper
if (-not (Test-Path ".\gradlew.bat")) {
    Write-Error "gradlew.bat not found in the current directory."
    Write-Host "Ensure you are running this script from the project root." -ForegroundColor Red
    return
}

function Clear-OldApks {
    $apkDir = "app\build\outputs\apk"
    if (Test-Path $apkDir) {
        Write-Host "Clearing old APKs..." -ForegroundColor Gray
        Remove-Item -Recurse -Force $apkDir
    }
}

function Show-ResultCheck {
    Write-Host "`n============================="
    $debugApk = "app\build\outputs\apk\debug\esl-tvplayer-debug.apk"
    $releaseApk = "app\build\outputs\apk\release\esl-tvplayer-release.apk"

    $found = $false
    if (Test-Path $debugApk) {
        Write-Host "DEBUG APK: $debugApk" -ForegroundColor White
        $found = $true
    }
    if (Test-Path $releaseApk) {
        Write-Host "RELEASE APK: $releaseApk" -ForegroundColor White
        Write-Host "Note: Release APK needs to be signed before installation." -ForegroundColor Yellow
        $found = $true
    }

    if (-not $found) {
        Write-Host "BUILD FAILED: No APKs generated." -ForegroundColor Red
    } else {
        Write-Host "`nBUILD SUCCESSFUL!" -ForegroundColor Green
    }
}

function Install-ToTV {
    Write-Host "`nConnecting to TV at $tvIp..." -ForegroundColor Yellow
    & $adb connect $tvIp
    
    $debugApk = "app\build\outputs\apk\debug\esl-tvplayer-debug.apk"
    if (Test-Path $debugApk) {
        Write-Host "Installing $debugApk to $tvIp..." -ForegroundColor Yellow
        
        # Capture output to check for signature mismatch
        $output = & $adb -s $tvIp install -r $debugApk 2>&1
        $outputString = $output | Out-String
        Write-Host $outputString

        if ($outputString -like "*INSTALL_FAILED_UPDATE_INCOMPATIBLE*") {
            Write-Host "Signature mismatch detected (previously installed version has a different signature)." -ForegroundColor Yellow
            $confirm = Read-Host "Would you like to uninstall the existing app and try again? (y/n)"
            if ($confirm -eq 'y') {
                Write-Host "Uninstalling $packageName..." -ForegroundColor Yellow
                & $adb -s $tvIp uninstall $packageName
                Write-Host "Retrying installation..." -ForegroundColor Yellow
                & $adb -s $tvIp install -r $debugApk
            }
        } elseif ($LASTEXITCODE -ne 0) {
            Write-Host "Installation failed." -ForegroundColor Red
        } else {
            Write-Host "Installation successful!" -ForegroundColor Green
        }
    } else {
        Write-Host "Debug APK not found. Please build the project first (Option 2)." -ForegroundColor Red
    }
}

function Clear-TVLogs {
    Write-Host "`nClearing logs on $tvIp..." -ForegroundColor Yellow
    & $adb -s $tvIp logcat -c
    Write-Host "Logs cleared." -ForegroundColor Green
}

function View-TVLogs {
    Write-Host "`nViewing logs for 'BrowserActivity' on $tvIp (Press Ctrl+C to stop)..." -ForegroundColor Yellow
    & $adb -s $tvIp logcat -v time | Select-String "BrowserActivity"
}

while($true) {
    Write-Host "`nChoose an option:" -ForegroundColor Cyan
    Write-Host "1. Clean project"
    Write-Host "2. Debug (Default)"
    Write-Host "3. Release"
    Write-Host "4. Both"
    Write-Host "5. Install to TV ($tvIp)"
    Write-Host "6. Clear TV Logs"
    Write-Host "7. View TV Logs (BrowserActivity)"
    Write-Host "8. Exit"
    $choice = Read-Host "Enter choice [1-8]"

    if ($choice -eq "1") {
        Write-Host "`nStep: Cleaning Project..." -ForegroundColor Yellow
        & .\gradlew.bat clean
    } elseif ($choice -eq "2" -or [string]::IsNullOrWhiteSpace($choice)) {
        Write-Host "`nStep: Building Debug..." -ForegroundColor Yellow
        Clear-OldApks
        & .\gradlew.bat assembleDebug
        if ($LASTEXITCODE -eq 0) {
            Show-ResultCheck
        } else {
            Write-Host "`nBUILD FAILED: Gradle returned exit code $LASTEXITCODE" -ForegroundColor Red
        }
    } elseif ($choice -eq "3") {
        Write-Host "`nStep: Building Release..." -ForegroundColor Yellow
        Clear-OldApks
        & .\gradlew.bat assembleRelease
        if ($LASTEXITCODE -eq 0) {
            Show-ResultCheck
        } else {
            Write-Host "`nBUILD FAILED: Gradle returned exit code $LASTEXITCODE" -ForegroundColor Red
        }
    } elseif ($choice -eq "4") {
        Write-Host "`nStep: Building Debug and Release..." -ForegroundColor Yellow
        Clear-OldApks
        & .\gradlew.bat assembleDebug assembleRelease
        if ($LASTEXITCODE -eq 0) {
            Show-ResultCheck
        } else {
            Write-Host "`nBUILD FAILED: Gradle returned exit code $LASTEXITCODE" -ForegroundColor Red
        }
    } elseif ($choice -eq "5") {
        Install-ToTV
    } elseif ($choice -eq "6") {
        Clear-TVLogs
    } elseif ($choice -eq "7") {
        View-TVLogs
    } elseif ($choice -eq "8") {
        Write-Host "Exiting..." -ForegroundColor Gray
        break
    } else {
        Write-Host "Invalid choice, please try again." -ForegroundColor Red
    }
}
