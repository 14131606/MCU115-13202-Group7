package com.example.currencybudgetapp

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.example.currencybudgetapp.databinding.ActivityUsageStatsBinding
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class UsageStatsActivity : AppCompatActivity() {
    private lateinit var binding: ActivityUsageStatsBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityUsageStatsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val tracker = UsageTracker(this)

        binding.tvOpenCount.text = "App 開啟次數:${tracker.getOpenCount()} 次"

        val last = tracker.getLastOpenTime()
        val sdf = SimpleDateFormat("yyyy/MM/dd HH:mm:ss", Locale.getDefault())
        binding.tvLastOpen.text =
            "最後開啟:" + if (last == 0L) "尚無紀錄" else sdf.format(Date(last))

        val prevClose = tracker.getPrevCloseTime()
        binding.tvLastClose.text =
            "上次結束:" + if (prevClose == 0L) "尚無紀錄" else sdf.format(Date(prevClose))

        val pages = tracker.getAllPageTimes()
        binding.tvPageTimes.text =
            if (pages.isEmpty()) "尚無紀錄"
            else pages.entries.sortedByDescending { it.value }
                .joinToString("\n") { "${it.key}:${formatTime(it.value)}" }

        val events = tracker.getEvents()
        binding.tvEvents.text =
            if (events.isEmpty()) "尚無紀錄" else events.reversed().joinToString("\n")

        binding.btnClear.setOnClickListener {
            tracker.clearEvents()
            recreate() // 重新整理畫面
        }
    }

    override fun onStop() {
        super.onStop()
        UsageTracker(this).recordAppClose()
    }

    // 毫秒轉成「X 分 Y 秒」
    private fun formatTime(ms: Long): String {
        val totalSec = ms / 1000
        return "${totalSec / 60} 分 ${totalSec % 60} 秒"
    }
}