package com.example.drugtestchecker

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent

class DismissReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent?) {
        val id = intent?.getStringExtra("reminder_id") ?: return
        val profileId = intent?.getStringExtra("profile_id")
        // look up the reminder message; if it indicates the user is required to test today,
        // and the profile hasn't acknowledged for today, schedule a retry in 10 minutes; otherwise do nothing
        val rems = ReminderStore.list(context)
        val found = rems.find { it.id == id }
        val today = java.time.ZonedDateTime.now(java.time.ZoneId.of("America/Boise")).toLocalDate().toString()
        if (found != null) {
            // if it's a scheduled test message, resurface notification after 10 minutes when dismissed
            if (found.message.contains("scheduled for a drug test today", true) ||
                found.message.contains("is scheduled for today", true) ||
                found.message.contains("a drug test is scheduled for today", true)) {
                val pid = profileId ?: found.profileId
                if (!ReminderStore.isAcknowledged(context, pid, today)) {
                    val mgr = AlarmScheduler(context)
                    mgr.scheduleInMinutes(10)
                }
            }
        }
    }
}
