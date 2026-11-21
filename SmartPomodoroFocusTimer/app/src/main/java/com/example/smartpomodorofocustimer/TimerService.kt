package com.example.smartpomodorofocustimer

import android.app.Service
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.Handler
import android.os.IBinder
import android.os.Looper
import android.widget.Toast
import androidx.core.content.ContextCompat

class TimerService : Service() {

    companion object {
        const val ACTION_START_TIMER = "com.example.smartpomodorofocustimer.ACTION_START"
        const val ACTION_STOP_TIMER = "com.example.smartpomodorofocustimer.ACTION_STOP"
        const val ACTION_TIMER_UPDATE = "com.example.smartpomodorofocustimer.TIMER_UPDATE"

        const val EXTRA_DURATION_SECONDS = "DURATION_SECONDS"
        const val EXTRA_REMAINING_SECONDS = "REMAINING_SECONDS"
        const val EXTRA_IS_RUNNING = "IS_RUNNING"

        const val DEFAULT_FOCUS_TIME_SECONDS = 1500 // 25 分鐘
        const val TOAST_INTERVAL_SECONDS = 120 // 每 2 分鐘提醒一次
    }

    private var remainingSeconds = DEFAULT_FOCUS_TIME_SECONDS

    @Volatile
    private var isRunning = false
    private var workerThread: Thread? = null
    private val mainHandler = Handler(Looper.getMainLooper())

    // 系統廣播接收器：監聽螢幕亮起 (ACTION_SCREEN_ON) 與耳機插拔
    private val systemBroadcastReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            if (!isRunning) return

            when (intent?.action) {
                Intent.ACTION_SCREEN_ON -> {
                    val minsLeft = (remainingSeconds + 59) / 60
                    val msg = if (minsLeft > 1) {
                        "保持專注！還剩 $minsLeft 分鐘"
                    } else if (minsLeft == 1) {
                        "保持專注！還剩 1 分鐘"
                    } else {
                        "保持專注！剩餘不到 1 分鐘"
                    }
                    showToastOnMainThread(msg)
                }
                Intent.ACTION_HEADSET_PLUG -> {
                    val state = intent.getIntExtra("state", -1)
                    if (state == 0) {
                        showToastOnMainThread("耳機已拔除，請繼續保持專注！")
                    } else if (state == 1) {
                        showToastOnMainThread("耳機已連接，進入專注模式！")
                    }
                }
            }
        }
    }

    override fun onCreate() {
        super.onCreate()
        val filter = IntentFilter().apply {
            addAction(Intent.ACTION_SCREEN_ON)
            addAction(Intent.ACTION_HEADSET_PLUG)
        }
        ContextCompat.registerReceiver(
            this,
            systemBroadcastReceiver,
            filter,
            ContextCompat.RECEIVER_EXPORTED
        )
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        when (intent?.action ?: ACTION_START_TIMER) {
            ACTION_START_TIMER -> {
                val duration = intent?.getIntExtra(
                    EXTRA_DURATION_SECONDS,
                    DEFAULT_FOCUS_TIME_SECONDS
                ) ?: DEFAULT_FOCUS_TIME_SECONDS
                startCountdownThread(duration)
            }
            ACTION_STOP_TIMER -> {
                stopCountdown()
            }
        }
        return START_NOT_STICKY
    }

    private fun startCountdownThread(durationSeconds: Int) {
        if (isRunning) return
        remainingSeconds = durationSeconds
        isRunning = true

        val initialMins = (remainingSeconds + 59) / 60
        showToastOnMainThread("專注開始！剩餘 $initialMins 分鐘")

        workerThread = Thread {
            var elapsed = 0
            while (isRunning && remainingSeconds > 0) {
                sendTimerBroadcast(remainingSeconds, true)

                // 每 2 分鐘底部顯示 Toast 提醒
                if (elapsed > 0 && elapsed % TOAST_INTERVAL_SECONDS == 0) {
                    val mins = remainingSeconds / 60
                    showToastOnMainThread("保持專注！剩 $mins 分鐘")
                }

                try {
                    Thread.sleep(1000L)
                } catch (e: InterruptedException) {
                    break
                }

                if (isRunning) {
                    remainingSeconds--
                    elapsed++
                }
            }

            // 計時器歸零：從背景啟動 FinishActivity 並呼叫 stopSelf()
            if (isRunning && remainingSeconds <= 0) {
                isRunning = false
                sendTimerBroadcast(0, false)
                showToastOnMainThread("焦點會議完成！")

                val finishIntent = Intent(this@TimerService, FinishActivity::class.java).apply {
                    addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP)
                }
                startActivity(finishIntent)
                stopSelf()
            }
        }.apply { start() }
    }

    private fun stopCountdown() {
        isRunning = false
        workerThread?.interrupt()
        workerThread = null
        sendTimerBroadcast(DEFAULT_FOCUS_TIME_SECONDS, false)
        showToastOnMainThread("已停止專注計時")
        stopSelf()
    }

    private fun sendTimerBroadcast(seconds: Int, running: Boolean) {
        val broadcastIntent = Intent(ACTION_TIMER_UPDATE).apply {
            putExtra(EXTRA_REMAINING_SECONDS, seconds)
            putExtra(EXTRA_IS_RUNNING, running)
            setPackage(packageName)
        }
        sendBroadcast(broadcastIntent)
    }

    private fun showToastOnMainThread(message: String) {
        mainHandler.post {
            Toast.makeText(applicationContext, message, Toast.LENGTH_SHORT).show()
        }
    }

    override fun onDestroy() {
        isRunning = false
        workerThread?.interrupt()
        try {
            unregisterReceiver(systemBroadcastReceiver)
        } catch (e: Exception) {
            // 忽略未註冊例外
        }
        super.onDestroy()
    }

    override fun onBind(intent: Intent?): IBinder? = null
}