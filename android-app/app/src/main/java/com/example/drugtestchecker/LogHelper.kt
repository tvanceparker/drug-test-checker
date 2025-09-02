package com.example.drugtestchecker

import android.content.Context
import android.util.Log
import java.io.File
import java.io.PrintWriter
import java.io.StringWriter
import java.time.ZoneId
import java.time.ZonedDateTime

object LogHelper {
    private const val TAG = "LogHelper"

    fun appendLog(ctx: Context, pin: String, last4: String, message: String) {
        try {
            val dir = ctx.filesDir
            if (dir == null) {
                Log.e(TAG, "filesDir is null")
                return
            }
            val f = File(dir, "drug_test_logs.csv")
            if (!f.exists()) f.writeText("timestamp,pin,last4,message\n")
            val ts = ZonedDateTime.now(ZoneId.of("America/Boise")).toString()
            f.appendText("${ts},${pin},${last4},\"${message.replace('"', ' ')}\"\n")
        } catch (e: Exception) {
            // Log exception to logcat and a small debug file so we can inspect failures from adb
            Log.e(TAG, "appendLog failed", e)
            try {
                val dbg = File(ctx.filesDir ?: return, "dtc_debug.txt")
                val sw = StringWriter()
                e.printStackTrace(PrintWriter(sw))
                dbg.appendText("[${ZonedDateTime.now(ZoneId.of("America/Boise"))}] appendLog error:\n${sw}\n")
            } catch (_: Exception) { /* ignore secondary failures */ }
        }
    }
}
