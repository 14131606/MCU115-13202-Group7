package com.example.currencybudgetapp

import android.content.Context

class UsageTracker(context: Context) {
    private val prefs = context.getSharedPreferences("usage", Context.MODE_PRIVATE)
    private var pageStart = 0L

    // App 開啟時呼叫:記錄開啟時間與次數
    fun recordAppOpen() {
        prefs.edit()
            .putLong("last_open_time", System.currentTimeMillis())
            .putInt("open_count", prefs.getInt("open_count", 0) + 1)
            .apply()
    }

    // 進入頁面時呼叫
    fun pageEnter() {
        pageStart = System.currentTimeMillis()
    }

    // 離開頁面時呼叫:把這次停留時間累加到該頁面
    fun pageExit(pageName: String) {
        if (pageStart == 0L) return
        val duration = System.currentTimeMillis() - pageStart
        pageStart = 0L
        val key = "page_$pageName"
        prefs.edit().putLong(key, prefs.getLong(key, 0L) + duration).apply()
    }

    fun getOpenCount(): Int = prefs.getInt("open_count", 0)
    fun getLastOpenTime(): Long = prefs.getLong("last_open_time", 0L)
    fun getPageTimeMs(pageName: String): Long = prefs.getLong("page_$pageName", 0L)

    // 取得所有頁面的累計停留時間(毫秒),方便其他組員顯示
    fun getAllPageTimes(): Map<String, Long> =
        prefs.all
            .filterKeys { it.startsWith("page_") }
            .map { it.key.removePrefix("page_") to (it.value as Long) }
            .toMap()
}