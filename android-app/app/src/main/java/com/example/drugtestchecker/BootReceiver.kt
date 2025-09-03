package com.example.drugtestchecker

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent

class BootReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent?) {
        if (intent?.action == Intent.ACTION_BOOT_COMPLETED) {
            val mgr = AlarmScheduler(context)
            val sp = context.getSharedPreferences("dtc", Context.MODE_PRIVATE)
            val hour = sp.getInt("schedule_hour", 3)
            val minute = sp.getInt("schedule_minute", 10)
            mgr.scheduleDailyAtLocal(hour, minute)
        }
    }
}
