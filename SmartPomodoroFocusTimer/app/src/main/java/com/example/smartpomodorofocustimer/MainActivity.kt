package com.example.smartpomodorofocustimer

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.graphics.Color
import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import java.util.Locale

class MainActivity : AppCompatActivity() {

    private lateinit var tvStatus: TextView
    private lateinit var tvTimer: TextView
    private lateinit var btnStart: Button
    private lateinit var btnStop: Button

    private var isReceiverRegistered = false

    private val timerUpdateReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            if (intent?.action == TimerService.ACTION_TIMER_UPDATE) {
                val remaining = intent.getIntExtra(
                    TimerService.EXTRA_REMAINING_SECONDS,
                    TimerService.DEFAULT_FOCUS_TIME_SECONDS
                )
                val running = intent.getBooleanExtra(TimerService.EXTRA_IS_RUNNING, false)

                updateTimerDisplay(remaining)
                updateStatusUI(running)
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        tvStatus = findViewById(R.id.tvStatus)
        tvTimer = findViewById(R.id.tvTimer)
        btnStart = findViewById(R.id.btnStart)
        btnStop = findViewById(R.id.btnStop)

        updateTimerDisplay(TimerService.DEFAULT_FOCUS_TIME_SECONDS)

        // 點擊 Start Focus：動態註冊 BroadcastReceiver 並呼叫 startService()
        btnStart.setOnClickListener {
            registerTimerReceiver()
            val startIntent = Intent(this, TimerService::class.java).apply {
                action = TimerService.ACTION_START_TIMER
                putExtra(TimerService.EXTRA_DURATION_SECONDS, TimerService.DEFAULT_FOCUS_TIME_SECONDS)
            }
            startService(startIntent)
            updateStatusUI(true)
        }

        // 點擊 STOP：直接呼叫 stopService() 停止背景服務並重設畫面
        btnStop.setOnClickListener {
            val stopIntent = Intent(this, TimerService::class.java)
            stopService(stopIntent)
            updateTimerDisplay(TimerService.DEFAULT_FOCUS_TIME_SECONDS)
            updateStatusUI(false)
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        if (isReceiverRegistered) {
            unregisterReceiver(timerUpdateReceiver)
            isReceiverRegistered = false
        }
    }

    private fun registerTimerReceiver() {
        if (!isReceiverRegistered) {
            val filter = IntentFilter(TimerService.ACTION_TIMER_UPDATE)
            ContextCompat.registerReceiver(
                this,
                timerUpdateReceiver,
                filter,
                ContextCompat.RECEIVER_NOT_EXPORTED
            )
            isReceiverRegistered = true
        }
    }

    private fun updateTimerDisplay(seconds: Int) {
        val mins = seconds / 60
        val secs = seconds % 60
        tvTimer.text = String.format(Locale.getDefault(), "%02d:%02d", mins, secs)
    }

    private fun updateStatusUI(isFocusing: Boolean) {
        if (isFocusing) {
            tvStatus.text = "Focusing..."
            tvStatus.setTextColor(Color.parseColor("#D32F2F"))
        } else {
            tvStatus.text = "Ready to Focus"
            tvStatus.setTextColor(Color.parseColor("#2E7D32"))
        }
    }
}