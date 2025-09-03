package com.example.drugtestchecker

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import java.time.*

class AlarmScheduler(val ctx: Context) {
    fun scheduleDailyAtLocal(hour: Int, minute: Int) {
        val zone = ZoneId.systemDefault()
        var now = ZonedDateTime.now(zone)
        var next = now.withHour(hour).withMinute(minute).withSecond(0).withNano(0)
        if (!next.isAfter(now)) next = next.plusDays(1)

        val epochMillis = next.toInstant().toEpochMilli()

        val am = ctx.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        val intent = Intent(ctx, AlarmReceiver::class.java)
        val pi = PendingIntent.getBroadcast(ctx, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE)

        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                if (am.canScheduleExactAlarms()) {
                    am.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, epochMillis, pi)
                } else {
                    am.set(AlarmManager.RTC_WAKEUP, epochMillis, pi)
                }
            } else {
                am.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, epochMillis, pi)
            }
        } catch (se: SecurityException) {
            am.set(AlarmManager.RTC_WAKEUP, epochMillis, pi)
        }
    }
    fun scheduleDailyAtBoise(hour: Int, minute: Int) {
        val zone = ZoneId.of("America/Boise")
        var now = ZonedDateTime.now(zone)
        var next = now.withHour(hour).withMinute(minute).withSecond(0).withNano(0)
        if (!next.isAfter(now)) next = next.plusDays(1)

        val epochMillis = next.toInstant().toEpochMilli()

        val am = ctx.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        val intent = Intent(ctx, AlarmReceiver::class.java)
        val pi = PendingIntent.getBroadcast(ctx, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE)

        // On Android S+ the app may not have the exact alarm permission. Avoid crashing by
        // falling back to a non-exact alarm when exact alarms aren't allowed.
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                if (am.canScheduleExactAlarms()) {
                    am.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, epochMillis, pi)
                } else {
                    am.set(AlarmManager.RTC_WAKEUP, epochMillis, pi)
                }
            } else {
                am.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, epochMillis, pi)
            }
        } catch (se: SecurityException) {
            // As a last-resort, attempt a non-exact set to avoid crashing the app.
            am.set(AlarmManager.RTC_WAKEUP, epochMillis, pi)
        }
    }

    fun scheduleAtMillis(epochMillis: Long) {
        val am = ctx.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        val intent = Intent(ctx, AlarmReceiver::class.java)
        val pi = PendingIntent.getBroadcast(ctx, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE)

        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                if (am.canScheduleExactAlarms()) {
                    am.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, epochMillis, pi)
                } else {
                    am.set(AlarmManager.RTC_WAKEUP, epochMillis, pi)
                }
            } else {
                am.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, epochMillis, pi)
            }
        } catch (se: SecurityException) {
            am.set(AlarmManager.RTC_WAKEUP, epochMillis, pi)
        }
    }

    fun scheduleInMinutes(minutes: Int) {
        val now = System.currentTimeMillis()
        scheduleAtMillis(now + minutes * 60 * 1000)
    }

    companion object {
        fun nextBoiseDailyMillis(hour: Int, minute: Int): Long {
            val zone = ZoneId.of("America/Boise")
            var now = ZonedDateTime.now(zone)
            var next = now.withHour(hour).withMinute(minute).withSecond(0).withNano(0)
            if (!next.isAfter(now)) next = next.plusDays(1)
            return next.toInstant().toEpochMilli()
        }
    }
}
