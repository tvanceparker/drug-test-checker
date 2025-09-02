package com.example.drugtestchecker

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent

class AcknowledgeReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent?) {
        val id = intent?.getStringExtra("reminder_id") ?: return
        // remove the reminder so notifications stop
        ReminderStore.remove(context, id)
        // cancel the notification
        try {
            val nm = context.getSystemService(android.content.Context.NOTIFICATION_SERVICE) as android.app.NotificationManager
            nm.cancel(id.hashCode())
        } catch (_: Exception) {}
        // also mark acknowledged for today's date for the profile id if provided
        val profileId = intent?.getStringExtra("profile_id")
        if (profileId != null) {
            val date = java.time.ZonedDateTime.now(java.time.ZoneId.of("America/Boise")).toLocalDate().toString()
            ReminderStore.markAcknowledged(context, profileId, date)
        }
        // also mark acknowledged for today's date for the profile id embedded in the reminder id mapping
        // reminder id is stored in prefs as reminder object; find it
        val rems = ReminderStore.list(context)
        val found = rems.find { it.id == id }
        if (found != null) {
            val profileId = found.profileId
            val date = java.time.ZonedDateTime.now(java.time.ZoneId.of("America/Boise")).toLocalDate().toString()
            ReminderStore.markAcknowledged(context, profileId, date)
        }
    }
}
