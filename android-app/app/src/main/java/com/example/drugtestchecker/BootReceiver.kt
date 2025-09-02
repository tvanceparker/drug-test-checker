package com.example.drugtestchecker

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent

class BootReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent?) {
        if (intent?.action == Intent.ACTION_BOOT_COMPLETED) {
            val mgr = AlarmScheduler(context)
            mgr.scheduleDailyAtBoise(3, 10)
        }
    }
}
