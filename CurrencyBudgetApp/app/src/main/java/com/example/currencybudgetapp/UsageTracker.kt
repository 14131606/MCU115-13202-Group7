package com.example.currencybudgetapp

import android.content.Context

class UsageTracker(context: Context) {
    private val prefs = context.getSharedPreferences("usage", Context.MODE_PRIVATE)
    private var pageStart = 0L

    // App 開啟時呼叫:記錄開啟時間與次數
    fun recordAppOpen() {
        // 開啟前,先把「上一次的結束時間」存起來,給畫面顯示用
        val lastClose = prefs.getLong("last_close_time", 0L)
        prefs.edit()
            .putLong("prev_close_time", lastClose)
            .putLong("last_open_time", System.currentTimeMillis())
            .putInt("open_count", prefs.getInt("open_count", 0) + 1)
            .apply()
    }

    // App 進入背景時呼叫:記錄結束時間
    fun recordAppClose() {
        prefs.edit().putLong("last_close_time", System.currentTimeMillis()).apply()
    }

    // 上一次使用結束的時間(0 代表沒有紀錄)
    fun getPrevCloseTime(): Long = prefs.getLong("prev_close_time", 0L)

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

    // 取得所有頁面的累計停留時間(毫秒)
    fun getAllPageTimes(): Map<String, Long> =
        prefs.all
            .filterKeys { it.startsWith("page_") }
            .map { it.key.removePrefix("page_") to (it.value as Long) }
            .toMap()

    // 記錄操作軌跡:例如 logEvent("按下新增記帳")
    fun logEvent(event: String) {
        val time = java.text.SimpleDateFormat("MM/dd HH:mm:ss", java.util.Locale.getDefault())
            .format(java.util.Date())
        val old = prefs.getString("events", "") ?: ""
        // 每筆一行,最新的放最後,最多保留 100 筆
        val lines = (if (old.isEmpty()) emptyList() else old.split("\n")) + "$time $event"
        prefs.edit().putString("events", lines.takeLast(100).joinToString("\n")).apply()
    }

    // 取得所有操作軌跡(由舊到新)
    fun getEvents(): List<String> {
        val s = prefs.getString("events", "") ?: ""
        return if (s.isEmpty()) emptyList() else s.split("\n")
    }

    fun clearEvents() = prefs.edit().remove("events").apply()
}