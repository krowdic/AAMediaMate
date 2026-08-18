# AAMediaMate - 繁簡轉換版本構建說明

此版本已添加台灣繁體中文歌詞轉換功能。

## 功能說明

- **自動繁簡轉換**: 根據系統語言環境自動轉換歌詞
- **台灣繁體支持**: 針對台灣地區用戶優化，自動將簡體中文轉換為台灣繁體
- **高精度轉換**: 使用 OpenCC4J 庫進行精確的繁簡轉換
- **無需手動設置**: 開箱即用，根據系統語言自動適配

## 構建要求

- **JDK**: Java 11 或更高版本
- **Android SDK**: API 29 或更高版本 (compileSdk 35)
- **Gradle**: 8.0 或更高版本 (推薦)

## 構建方式

### 方式一: 使用 Android Studio (推薦)

1. 克隆或打開本項目
2. 等待 Gradle 同步完成
3. 選擇 Build → Build Bundle(s) / APK(s) → Build APK(s)
4. APK 將生成在 `app/build/outputs/apk/debug/` 目錄

### 方式二: 使用命令行

```bash
# 構建 Debug APK
./gradlew assembleDebug

# 或構建 Release APK (需要簽名配置)
./gradlew assembleRelease
```

### 方式三: 使用提供的構建腳本

```bash
# Linux/macOS
chmod +x build.sh
./build.sh

# Windows
build.bat
```

## 輸出文件位置

- **Debug APK**: `app/build/outputs/apk/debug/app-debug.apk`
- **Release APK**: `app/build/outputs/apk/release/app-release.apk`

## 安裝 APK

### 開發設備 (需要 USB 偵錯)

```bash
adb install app/build/outputs/apk/debug/app-debug.apk
```

### 通過 Android Studio

1. Build → Build APK(s) 完成後
2. 點擊「Analyze APK」或直接安裝到連接的設備

## 核心變更

### 新增文件
- `app/src/main/java/com/gululu/aamediamate/utils/ChineseConverter.kt` - 繁簡轉換工具類

### 修改文件
- `app/src/main/java/com/gululu/aamediamate/LyricDisplayManager.kt` - 添加轉換邏輯

### 依賴
- `com.github.houbb:opencc4j:1.6.0` (已在 build.gradle.kts 中包含)

## 轉換邏輯

系統將根據設備語言環境自動決定：

- **台灣 (zh-TW)**: 自動轉換為台灣繁體中文
- **香港 (zh-HK)**: 自動轉換為香港繁體中文  
- **澳門 (zh-MO)**: 自動轉換為香港繁體中文
- **繁體中文 (script=Hant)**: 自動轉換為繁體中文
- **其他**: 保持原文或轉換為簡體中文

## 問題排查

### APK 构建失败

1. 確保 JDK 版本 ≥ 11
   ```bash
   java -version
   ```

2. 清理 Gradle 緩存
   ```bash
   ./gradlew clean
   ```

3. 重新同步依賴
   ```bash
   ./gradlew sync
   ```

### 轉換效果不如預期

- 檢查系統語言設置 (設定 → 系統 → 語言)
- 確保應用有足夠權限讀取系統語言配置
- 某些特殊詞彙可能需要手動編輯

## 版本信息

- **應用版本**: 1.4.1
- **最低 SDK**: 29 (Android 10)
- **目標 SDK**: 35 (Android 15)
- **語言**: Kotlin

## 許可證

見 LICENSE 文件

## 支持

有任何問題，請在 GitHub 上報告 Issue。
