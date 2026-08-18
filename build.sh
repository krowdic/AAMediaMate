#!/bin/bash

# AAMediaMate 繁簡轉換版本 - 構建腳本 (Linux/macOS)

set -e

echo "================================"
echo "AAMediaMate 構建開始"
echo "================================"
echo ""

# 檢查必要工具
if ! command -v java &> /dev/null; then
    echo "❌ 錯誤: 未找到 Java，請安裝 JDK 11 或更高版本"
    exit 1
fi

JAVA_VERSION=$(java -version 2>&1 | awk -F'"' '{print $2}' | awk -F'.' '{print $1}')
if [ "$JAVA_VERSION" -lt 11 ]; then
    echo "❌ 錯誤: Java 版本 $JAVA_VERSION，需要 11 或更高版本"
    exit 1
fi

echo "✅ Java 版本: $JAVA_VERSION"
echo ""

# 清理之前的構建
echo "🧹 清理舊構建文件..."
./gradlew clean

# 構建 Debug APK
echo ""
echo "🔨 正在構建 Debug APK..."
./gradlew assembleDebug

# 檢查構建結果
if [ -f "app/build/outputs/apk/debug/app-debug.apk" ]; then
    APK_SIZE=$(ls -lh app/build/outputs/apk/debug/app-debug.apk | awk '{print $5}')
    echo ""
    echo "================================"
    echo "✅ 構建成功！"
    echo "================================"
    echo "APK 文件位置: app/build/outputs/apk/debug/app-debug.apk"
    echo "文件大小: $APK_SIZE"
    echo ""
    echo "安裝到設備:"
    echo "  adb install app/build/outputs/apk/debug/app-debug.apk"
    echo ""
else
    echo ""
    echo "❌ 構建失敗，未找到 APK 文件"
    exit 1
fi