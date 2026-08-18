@echo off
REM AAMediaMate - Build Script for Windows

setlocal enabledelayedexpansion

echo ========================================
echo AAMediaMate Build Started
echo ========================================
echo.

REM Check if Java is installed
java -version >nul 2>&1
if errorlevel 1 (
    echo ERROR: Java not found. Please install JDK 11 or higher.
    exit /b 1
)

echo [OK] Java found
echo.

REM Clean previous build
echo [*] Cleaning old build files...
call gradlew.bat clean

REM Build Debug APK
echo.
echo [*] Building Debug APK...
call gradlew.bat assembleDebug

REM Check if build was successful
if exist "app\build\outputs\apk\debug\app-debug.apk" (
    echo.
    echo ========================================
    echo [SUCCESS] Build completed!
    echo ========================================
    echo APK Location: app\build\outputs\apk\debug\app-debug.apk
    echo.
    echo Install to device:
    echo   adb install app\build\outputs\apk\debug\app-debug.apk
    echo.
) else (
    echo.
    echo [ERROR] Build failed - APK not found
    exit /b 1
)

endlocal