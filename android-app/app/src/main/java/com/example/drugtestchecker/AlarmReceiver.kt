package com.example.drugtestchecker

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat
import org.jsoup.Jsoup
import java.io.File
import java.time.ZoneId
import java.time.ZonedDateTime

class AlarmReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent?) {
        Thread {
            try {
                // debug override: if the broadcast includes debug_force_required=true, skip network and force a required result
                val forceRequired = intent?.getBooleanExtra("debug_force_required", false) ?: false
                if (forceRequired) {
                    val pin = context.getSharedPreferences("dtc", Context.MODE_PRIVATE).getString("pin", "") ?: ""
                    val last4 = context.getSharedPreferences("dtc", Context.MODE_PRIVATE).getString("last4", "") ?: ""
                    val message = "You are required to test today (debug)"
                    val profileId = "${pin}_${last4}"
                    val remId = System.currentTimeMillis().toString()
                    LogHelper.appendLog(context, pin, last4, message)
                    val today = java.time.ZonedDateTime.now(ZoneId.of("America/Boise")).toLocalDate().toString()
                    if (!ReminderStore.isAcknowledged(context, profileId, today)) {
                        ReminderStore.add(context, Reminder(remId, profileId, message, System.currentTimeMillis()))
                        this@AlarmReceiver.showNotificationWithActions(context, "Drug Test Checker", message, remId, profileId, true)
                    }
                    // reschedule and exit
                    val mgr = AlarmScheduler(context)
                    mgr.scheduleDailyAtBoise(3, 10)
                    return@Thread
                }
                val prefs = context.getSharedPreferences("dtc", Context.MODE_PRIVATE)
                val pin = prefs.getString("pin", "") ?: ""
                val last4 = prefs.getString("last4", "") ?: ""

                // Do not proceed without creds
                if (pin.isBlank() || last4.isBlank()) return@Thread

                val doc = Jsoup.connect("https://drugtestcheck.com/")
                    .userAgent("Mozilla/5.0 (Android)")
                    .timeout(10000)
                    .data("callInCode", pin, "lastName", last4)
                    .post()

                val labels = doc.select("label").map { it.text() }
                var message = "No recognizable response"
                var required = false
                var dateText: String? = null
                // regex to capture required/not required and optional date after comma
                val reqRegex = Regex("(?i)you are\\s+required[^\\n]*to test today(?:,?\\s*(.*))?")
                val notReqRegex = Regex("(?i)you are\\s+not\\s+required[^\\n]*to test today(?:,?\\s*(.*))?")
                for (t in labels) {
                    val tr = t.trim()
                    val mr = reqRegex.find(tr)
                    if (mr != null) {
                        message = tr
                        required = true
                        dateText = mr.groupValues.getOrNull(1)?.trim()?.trimEnd('.')
                        break
                    }
                    val mn = notReqRegex.find(tr)
                    if (mn != null) {
                        message = tr
                        required = false
                        dateText = mn.groupValues.getOrNull(1)?.trim()?.trimEnd('.')
                        break
                    }
                    if (tr.contains("Please try again during your agency's call-in timeframe", true)) { message = tr; break }
                }

                // If nothing matched, dump labels + a small HTML snapshot to debug file for diagnosis
                if (message == "No recognizable response") {
                    try {
                        val dir = context.filesDir ?: return@Thread
                        val dbg = File(dir, "dtc_debug.txt")
                        val ts = ZonedDateTime.now(ZoneId.of("America/Boise")).toString()
                        val sb = StringBuilder()
                        sb.append("[").append(ts).append("] Unrecognized response labels:\n")
                        for (l in labels) sb.append("- ").append(l).append("\n")
                        sb.append("--- HTML snippet start ---\n")
                        // write a truncated snapshot to avoid huge files
                        val html = doc.html()
                        sb.append(html.take(2000)).append("\n")
                        sb.append("--- HTML snippet end ---\n\n")
                        dbg.appendText(sb.toString())
                    } catch (_: Exception) { /* ignore debug write failures */ }
                }

                // log
                LogHelper.appendLog(context, pin, last4, message)

                    // create profile id and reminder entry
                    val profileId = "${pin}_${last4}"
                    val remId = System.currentTimeMillis().toString()
                    // only add reminder / notify if not already acknowledged for today
                    val today = java.time.ZonedDateTime.now(ZoneId.of("America/Boise")).toLocalDate().toString()
                    if (!ReminderStore.isAcknowledged(context, profileId, today)) {
                        ReminderStore.add(context, Reminder(remId, profileId, message, System.currentTimeMillis()))
                        // notify with actions; if required then keep notification persistent until acknowledged
                            this@AlarmReceiver.showNotificationWithActions(context, "Drug Test Checker", message, remId, profileId, required)
                    }

                // reschedule for next day
                val mgr = AlarmScheduler(context)
                mgr.scheduleDailyAtBoise(3, 10)
            } catch (e: Exception) {
                // Log exception to logcat and append to debug file
                try {
                    android.util.Log.e("AlarmReceiver", "check failed", e)
                    val dir = context.filesDir ?: return@Thread
                    val dbg = java.io.File(dir, "dtc_debug.txt")
                    val ts = ZonedDateTime.now(ZoneId.of("America/Boise")).toString()
                    val sw = java.io.StringWriter()
                    e.printStackTrace(java.io.PrintWriter(sw))
                    val sb = StringBuilder()
                    sb.append('[').append(ts).append(']').append(" alarm error:\n")
                    sb.append(sw.toString()).append('\n')
                    dbg.appendText(sb.toString())
                } catch (_: Exception) { /* ignore secondary failures */ }
            }
        }.start()
    }

    fun showNotification(ctx: Context, title: String, body: String) {
        val nm = ctx.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        val channelId = "dtc_channel"
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val ch = NotificationChannel(channelId, "Drug Test Checker", NotificationManager.IMPORTANCE_HIGH)
            nm.createNotificationChannel(ch)
        }
        val notif = NotificationCompat.Builder(ctx, channelId)
            .setContentTitle(title)
            .setContentText(body)
            .setSmallIcon(android.R.drawable.ic_dialog_info)
            .build()
        nm.notify(1001, notif)
    }

    fun showNotificationWithActions(ctx: Context, title: String, body: String, reminderId: String, profileId: String, required: Boolean) {
        val nm = ctx.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        val channelId = "dtc_channel"
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val ch = NotificationChannel(channelId, "Drug Test Checker", NotificationManager.IMPORTANCE_HIGH)
            nm.createNotificationChannel(ch)
        }

        val ackIntent = Intent(ctx, AcknowledgeReceiver::class.java)
            .putExtra("reminder_id", reminderId)
            .putExtra("profile_id", profileId)
        val ackPi = PendingIntent.getBroadcast(ctx, reminderId.hashCode(), ackIntent, PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE)

        val dismissIntent = Intent(ctx, DismissReceiver::class.java)
            .putExtra("reminder_id", reminderId)
            .putExtra("profile_id", profileId)
        val dismissPi = PendingIntent.getBroadcast(ctx, reminderId.hashCode()+1, dismissIntent, PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE)

        val builder = NotificationCompat.Builder(ctx, channelId)
            .setContentTitle(title)
            .setContentText(body)
            .setSmallIcon(android.R.drawable.ic_dialog_info)
            .addAction(android.R.drawable.ic_menu_send, "Acknowledge", ackPi)

        if (required) {
            // persistent ongoing notification until acknowledged
            builder.setOngoing(true)
        } else {
            builder.setDeleteIntent(dismissPi)
        }

        val notif = builder.build()
        nm.notify(reminderId.hashCode(), notif)
    }
}
