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
import java.time.format.DateTimeFormatter
import java.util.Locale

class AlarmReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent?) {
        Thread {
            try {
                val prefs = context.getSharedPreferences("dtc", Context.MODE_PRIVATE)
                val pin = prefs.getString("pin", "") ?: ""
                val last4 = prefs.getString("last4", "") ?: ""

                // Do not proceed without creds
                if (pin.isBlank() || last4.isBlank()) return@Thread

                // prepare identifiers for this run
                val profileId = "${pin}_${last4}"
                val remId = System.currentTimeMillis().toString()

                // debug override: if the broadcast includes debug_force_required=true, skip network and force a required result
                val forceRequired = intent?.getBooleanExtra("debug_force_required", false) ?: false
                if (forceRequired) {
                    val message = "You are required to test today (debug)"
                    // fetch real HTML so the snapshot reflects the site response
                    var tsName = ZonedDateTime.now(ZoneId.of("America/Boise")).format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss"))
                    try {
                        val doc = Jsoup.connect("https://drugtestcheck.com/")
                            .userAgent("Mozilla/5.0 (Android)")
                            .timeout(10000)
                            .data("callInCode", pin, "lastName", last4)
                            .post()
                        val dir = context.filesDir ?: return@Thread
                        val htmlDir = File(dir, "html")
                        if (!htmlDir.exists()) htmlDir.mkdirs()
                        tsName = ZonedDateTime.now(ZoneId.of("America/Boise")).format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss"))
                        val snapName = "snapshot_${remId}_$tsName.html"
                        val snap = File(htmlDir, snapName)
                        val html = doc.outerHtml()
                        val toWrite = if (html.length > 200_000) html.substring(0, 200_000) else html
                        snap.writeText(toWrite)
                        // append index entry
                        val idx = File(htmlDir, "index.txt")
                        val idxLine = listOf(snapName, tsName, profileId, message.replace('\n',' ')).joinToString("|") + "\n"
                        idx.appendText(idxLine)
                    } catch (_: Exception) { /* ignore snapshot failures in debug */ }

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

                val doc = Jsoup.connect("https://drugtestcheck.com/")
                    .userAgent("Mozilla/5.0 (Android)")
                    .timeout(10000)
                    .data("callInCode", pin, "lastName", last4)
                    .post()

                val labels = doc.select("label").map { it.text() }
                var message = "No recognizable response"
                var required = false
                var dateText: String? = null
                // regex to capture the site's actual phrasing and optional date after comma
                val reqRegex = Regex("(?i)you\\s+are\\s+scheduled\\s+for\\s+a\\s+drug\\s+test\\s+today(?:,?\\s*(.*))?")
                val notReqRegex = Regex("(?i)a\\s+drug\\s+test\\s+is\\s+not\\s+scheduled\\s+for\\s+today(?:,?\\s*(.*))?")
                for (t in labels) {
                    val tr = t.trim()
                    val mr = reqRegex.find(tr)
                    if (mr != null) {
                        required = true
                        dateText = mr.groupValues.getOrNull(1)?.trim()?.trimEnd('.')
                        // we'll format the user-facing message after parsing
                        break
                    }
                    val mn = notReqRegex.find(tr)
                    if (mn != null) {
                        required = false
                        dateText = mn.groupValues.getOrNull(1)?.trim()?.trimEnd('.')
                        // we'll format the user-facing message after parsing
                        break
                    }
                    if (tr.contains("Please try again during your agency's call-in timeframe", true)) { message = tr; break }
                }

                // If we matched required/not-required, construct a standardized message
                if (message != "No recognizable response" && !message.startsWith("Please try again", true)) {
                    // message may already have been set to the raw label; prefer our standardized wording
                }
                if (message == "No recognizable response") {
                    // check if we parsed required/not-required (required flag updated)
                    if (required || dateText != null) {
                        val formatter = DateTimeFormatter.ofPattern("MMMM dd", Locale.ENGLISH)
                        val dateString = if (!dateText.isNullOrBlank()) {
                            // ensure month name capitalization
                            dateText.replaceFirstChar { if (it.isLowerCase()) it.titlecase(Locale.ENGLISH) else it.toString() }
                        } else {
                            ZonedDateTime.now(ZoneId.of("America/Boise")).format(formatter)
                        }
                        message = if (required) {
                            "a drug test is scheduled for today, $dateString."
                        } else {
                            "a drug test is not scheduled for today, $dateString."
                        }
                    }
                } else {
                    // If message was set earlier e.g., "Please try again...", leave it as-is.
                }

                // Save an HTML snapshot for every run (up to 200KB to keep files small)
                try {
                    val dir = context.filesDir ?: return@Thread
                    val htmlDir = File(dir, "html")
                    if (!htmlDir.exists()) htmlDir.mkdirs()
                    val tsName = ZonedDateTime.now(ZoneId.of("America/Boise")).format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss"))
                    val snapName = "snapshot_${remId}_$tsName.html"
                    val snap = File(htmlDir, snapName)
                    // write entire HTML; viewers will load this file
                    val html = doc.outerHtml()
                    // limit to ~200KB to avoid very large files
                    val toWrite = if (html.length > 200_000) html.substring(0, 200_000) else html
                    snap.writeText(toWrite)
                    // append an index entry for viewer: simple CSV: filename|timestamp|profileId|message
                    val idx = File(htmlDir, "index.txt")
                    val idxLine = listOf(snapName, tsName, profileId, message.replace('\n',' ')).joinToString("|") + "\n"
                    idx.appendText(idxLine)
                } catch (_: Exception) { /* ignore snapshot write failures */ }

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

                    // create reminder entry (profileId and remId prepared earlier)
                    // only add reminder / notify if not already acknowledged for today
                    val today = java.time.ZonedDateTime.now(ZoneId.of("America/Boise")).toLocalDate().toString()
                    if (!ReminderStore.isAcknowledged(context, profileId, today)) {
                        ReminderStore.add(context, Reminder(remId, profileId, message, System.currentTimeMillis()))
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
            // always set delete intent so swiping triggers DismissReceiver and can reschedule
            .setDeleteIntent(dismissPi)
            .setAutoCancel(true)

        val notif = builder.build()
        nm.notify(reminderId.hashCode(), notif)
    }
}
