package com.example.threadhandlerandprogressbarwithbackgroundmusic

import android.app.Application
import android.media.MediaPlayer
import android.os.Handler
import android.os.HandlerThread
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData

class WorkViewModel(application: Application) : AndroidViewModel(application) {
    private val handlerThread = HandlerThread("VM-Work").apply { start() }
    private val worker = Handler(handlerThread.looper)

    private val _progress = MutableLiveData(0)
    val progress: LiveData<Int> = _progress

    private val _status = MutableLiveData("Idle")
    val status: LiveData<String> = _status

    @Volatile private var running = false
    private var mediaPlayer: MediaPlayer? = null

    fun start() {
        if (running) return
        running = true
        _status.postValue("準備中...")
        _progress.postValue(0)

        worker.post {
            try {
                // 準備階段（3 秒）
                Thread.sleep(3000)
                if (!running) return@post

                // 開始背景工作並播放 callin.mp3
                _status.postValue("工作中...")
                startMusic()

                for (i in 1..100) {
                    if (!running) break
                    Thread.sleep(100)
                    _progress.postValue(i)
                }

                if (running) {
                    _status.postValue("背景工作結束！")
                } else {
                    _status.postValue("Canceled")
                }
            } catch (_: InterruptedException) {
                _status.postValue("Canceled")
            } finally {
                running = false
                stopMusic()
            }
        }
    }

    fun cancel() {
        running = false
        stopMusic()
        _status.postValue("Canceled")
    }

    private fun startMusic() {
        try {
            if (mediaPlayer == null) {
                mediaPlayer = MediaPlayer.create(getApplication(), R.raw.analogue_radio)?.apply {
                    isLooping = true
                    seekTo(11_000)
                    start()
                }
            } else if (mediaPlayer?.isPlaying == false) {
                mediaPlayer?.start()
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun stopMusic() {
        try {
            mediaPlayer?.let {
                if (it.isPlaying) it.stop()
                it.release()
            }
            mediaPlayer = null
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    override fun onCleared() {
        running = false
        stopMusic()
        handlerThread.quitSafely()
        super.onCleared()
    }
}