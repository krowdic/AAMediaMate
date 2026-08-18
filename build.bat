@echo off
REM AAMediaMate 繁簡轉換版本 - 構建腳本 (Windows)

setlocal enabledelayedexpansion

echo ================================
echo AAMediaMate 構建開始
echo ================================
echo.

REM 檢查 Java
java -version >nul 2>&1
if errorlevel 1 (
    echo ❌ 錯誤: 未找到 Java，請安裝 JDK 11 或更高版本
    exit /b 1
)

for /f tokens=3 %%i in ('java -version 2^>^&1 ^| find /i "version"') do (
    set JAVA_VER=%%i
)

echo ✅ Java 版本: %JAVA_VER%
echo.

REM 清理之前的構建
echo 🧹 清理舊構建文件...
call gradlew.bat clean

REM 構建 Debug APK
echo.
echo 🔨 正在構建 Debug APK...
call gradlew.bat assembleDebug

REM 檢查構建結果
if exist "app\build\outputs\apk\debug\app-debug.apk" (
    echo.
    echo ================================
    echo ✅ 構建成功！
    echo ================================
    echo APK 文件位置: app\build\outputs\apk\debug\app-debug.apk
    echo.
    echo 安裝到設備:
    echo   adb install app\build\outputs\apk\debug\app-debug.apk
    echo.
) else (
    echo.
    echo ❌ 構建失敗，未找到 APK 文件
    exit /b 1
)

endlocal