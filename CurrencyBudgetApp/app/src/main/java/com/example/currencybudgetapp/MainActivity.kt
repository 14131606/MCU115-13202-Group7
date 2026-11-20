package com.example.currencybudgetapp

import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.setupWithNavController
import com.example.currencybudgetapp.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding
    private lateinit var tracker: UsageTracker
    private var currentPage = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val navHostFragment = supportFragmentManager
            .findFragmentById(R.id.fragment_container) as NavHostFragment
        val navController = navHostFragment.navController
        binding.bottomNav.setupWithNavController(navController)

        // 使用紀錄
        tracker = UsageTracker(this)
        if (savedInstanceState == null) tracker.recordAppOpen() // 轉螢幕不重複計算

        // 每次切換頁面:結算上一頁的停留時間,開始計時新的一頁
        navController.addOnDestinationChangedListener { _, destination, _ ->
            if (currentPage.isNotEmpty()) tracker.pageExit(currentPage)
            currentPage = resources.getResourceEntryName(destination.id)
            tracker.pageEnter()
        }
    }

    // App 回到前景:重新開始計時
    override fun onStart() {
        super.onStart()
        tracker.pageEnter()
    }

    // App 到背景或關閉:結算目前頁面的停留時間
    override fun onStop() {
        super.onStop()
        if (currentPage.isNotEmpty()) tracker.pageExit(currentPage)
        Log.d("Usage", "開啟次數=${tracker.getOpenCount()}, 各頁面毫秒=${tracker.getAllPageTimes()}")
    }
}
