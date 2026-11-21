package com.example.smartpomodorofocustimer

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.widget.Toast

class BootReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action == Intent.ACTION_BOOT_COMPLETED) {
            Toast.makeText(
                context,
                "Device Rebooted: Don't forget your daily Pomodoro focus session!",
                Toast.LENGTH_LONG
            ).show()
        }
    }
}