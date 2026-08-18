package com.gululu.aamediamate.utils

import android.content.Context
import android.util.Log
import com.github.houbb.opencc4j.util.ZhConverterUtil

/**
 * 繁簡中文轉換工具
 * 使用 OpenCC4J 庫進行高精度的繁簡轉換
 */
object ChineseConverter {
    private const val TAG = "ChineseConverter"
    
    // 轉換模式
    enum class ConversionMode {
        /** 保持原樣，不進行轉換 */
        NONE,
        /** 轉換為簡體中文 */
        TO_SIMPLIFIED,
        /** 轉換為繁體中文（台灣) */
        TO_TRADITIONAL_TW,
        /** 轉換為繁體中文（香港) */
        TO_TRADITIONAL_HK
    }
    
    /**
     * 根據系統語言環境自動決定轉換模式
     * @param context Android 上下文
     * @return 轉換模式
     */
    fun getConversionModeByLocale(context: Context): ConversionMode {
        val locale = context.resources.configuration.locales.get(0)
        
        return when {
            // 台灣地區
            locale.language == "zh" && locale.country == "TW" -> ConversionMode.TO_TRADITIONAL_TW
            // 香港地區
            locale.language == "zh" && locale.country == "HK" -> ConversionMode.TO_TRADITIONAL_HK
            // 澳門地區 (使用香港轉換規則)
            locale.language == "zh" && locale.country == "MO" -> ConversionMode.TO_TRADITIONAL_HK
            // 繁體中文 (script = Hant)
            locale.language == "zh" && locale.script == "Hant" -> ConversionMode.TO_TRADITIONAL_TW
            // 簡體中文或其他
            else -> ConversionMode.TO_SIMPLIFIED
        }
    }
    
    /**
     * 轉換文本
     * @param text 要轉換的文本
     * @param mode 轉換模式
     * @return 轉換後的文本
     */
    fun convert(text: String, mode: ConversionMode): String {
        if (text.isBlank() || mode == ConversionMode.NONE) {
            return text
        }
        
        return try {
            when (mode) {
                ConversionMode.TO_SIMPLIFIED -> {
                    // 轉換為簡體中文
                    ZhConverterUtil.toSimplified(text)
                }
                ConversionMode.TO_TRADITIONAL_TW -> {
                    // 轉換為繁體中文（台灣)
                    ZhConverterUtil.toTraditional(text)
                }
                ConversionMode.TO_TRADITIONAL_HK -> {
                    // 轉換為繁體中文（香港)
                    // OpenCC4J 沒有直接的香港轉換，使用繁體轉換
                    ZhConverterUtil.toTraditional(text)
                }
                ConversionMode.NONE -> text
            }
        } catch (e: Exception) {
            Log.e(TAG, "轉換失敗: ${e.message}", e)
            // 如果轉換失敗，返回原文本
            text
        }
    }
    
    /**
     * 批量轉換文本列表
     * @param texts 文本列表
     * @param mode 轉換模式
     * @return 轉換後的文本列表
     */
    fun convertList(texts: List<String>, mode: ConversionMode): List<String> {
        if (mode == ConversionMode.NONE) {
            return texts
        }
        return texts.map { convert(it, mode) }
    }
    
    /**
     * 獲取用戶偏好的轉換模式
     * @param context Android 上下文
     * @param conversionModeString 保存的轉換模式字符串 (見 SettingsManager)
     * @return 轉換模式，如果無效則返回根據語言環境自動決定的模式
     */
    fun getConversionMode(context: Context, conversionModeString: String): ConversionMode {
        if (conversionModeString.isEmpty()) {
            return getConversionModeByLocale(context)
        }
        
        return try {
            ConversionMode.valueOf(conversionModeString)
        } catch (e: IllegalArgumentException) {
            Log.w(TAG, "無效的轉換模式字符串: $conversionModeString，使用自動模式")
            getConversionModeByLocale(context)
        }
    }
}
