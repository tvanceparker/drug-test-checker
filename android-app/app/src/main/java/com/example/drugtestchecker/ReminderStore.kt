package com.example.drugtestchecker

import android.content.Context
import org.json.JSONArray
import org.json.JSONObject

data class Reminder(val id: String, val profileId: String, val message: String, val ts: Long)

object ReminderStore {
    private const val KEY = "dtc_reminders"
    private const val ACK_KEY = "dtc_acks"

    fun add(ctx: Context, r: Reminder) {
        val prefs = ctx.getSharedPreferences("dtc", Context.MODE_PRIVATE)
        val arr = JSONArray(prefs.getString(KEY, "[]"))
        arr.put(JSONObject().put("id", r.id).put("profileId", r.profileId).put("message", r.message).put("ts", r.ts))
        prefs.edit().putString(KEY, arr.toString()).apply()
    }

    fun remove(ctx: Context, id: String) {
        val prefs = ctx.getSharedPreferences("dtc", Context.MODE_PRIVATE)
        val arr = JSONArray(prefs.getString(KEY, "[]"))
        val out = JSONArray()
        for (i in 0 until arr.length()) {
            val o = arr.getJSONObject(i)
            if (o.getString("id") != id) out.put(o)
        }
        prefs.edit().putString(KEY, out.toString()).apply()
    }

    fun list(ctx: Context): List<Reminder> {
        val prefs = ctx.getSharedPreferences("dtc", Context.MODE_PRIVATE)
        val arr = JSONArray(prefs.getString(KEY, "[]"))
        val out = mutableListOf<Reminder>()
        for (i in 0 until arr.length()) {
            val o = arr.getJSONObject(i)
            out.add(Reminder(o.getString("id"), o.getString("profileId"), o.getString("message"), o.getLong("ts")))
        }
        return out
    }

    fun markAcknowledged(ctx: Context, profileId: String, date: String) {
        val prefs = ctx.getSharedPreferences("dtc", Context.MODE_PRIVATE)
        val arr = JSONArray(prefs.getString(ACK_KEY, "[]"))
        arr.put(JSONObject().put("profileId", profileId).put("date", date))
        prefs.edit().putString(ACK_KEY, arr.toString()).apply()
    }

    fun isAcknowledged(ctx: Context, profileId: String, date: String): Boolean {
        val prefs = ctx.getSharedPreferences("dtc", Context.MODE_PRIVATE)
        val arr = JSONArray(prefs.getString(ACK_KEY, "[]"))
        for (i in 0 until arr.length()) {
            val o = arr.getJSONObject(i)
            if (o.getString("profileId") == profileId && o.getString("date") == date) return true
        }
        return false
    }
}
