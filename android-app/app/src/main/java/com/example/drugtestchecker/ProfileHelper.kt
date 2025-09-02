package com.example.drugtestchecker

import android.content.Context
import org.json.JSONArray
import org.json.JSONObject

data class Profile(val id: String, val name: String, val pin: String, val last4: String)

object ProfileHelper {
    private const val KEY = "dtc_profiles"

    fun saveProfile(ctx: Context, profile: Profile) {
        val prefs = ctx.getSharedPreferences("dtc", Context.MODE_PRIVATE)
        val raw = prefs.getString(KEY, "[]") ?: "[]"
        val arr = JSONArray(raw)
        // replace if id exists
        var found = false
        for (i in 0 until arr.length()) {
            val o = arr.getJSONObject(i)
            if (o.getString("id") == profile.id) { arr.put(i, JSONObject().put("id", profile.id).put("name", profile.name).put("pin", profile.pin).put("last4", profile.last4)); found = true; break }
        }
        if (!found) arr.put(JSONObject().put("id", profile.id).put("name", profile.name).put("pin", profile.pin).put("last4", profile.last4))
        prefs.edit().putString(KEY, arr.toString()).apply()
    }

    fun addNewProfile(ctx: Context, name: String, pin: String, last4: String): Profile {
        val id = System.currentTimeMillis().toString()
        val p = Profile(id, name, pin, last4)
        saveProfile(ctx, p)
        return p
    }

    fun getProfiles(ctx: Context): List<Profile> {
        val prefs = ctx.getSharedPreferences("dtc", Context.MODE_PRIVATE)
        val raw = prefs.getString(KEY, "[]") ?: "[]"
        val arr = JSONArray(raw)
        val out = mutableListOf<Profile>()
        for (i in 0 until arr.length()) {
            val o = arr.getJSONObject(i)
            out.add(Profile(o.getString("id"), o.getString("name"), o.getString("pin"), o.getString("last4")))
        }
        return out
    }

    fun deleteProfile(ctx: Context, id: String) {
        val prefs = ctx.getSharedPreferences("dtc", Context.MODE_PRIVATE)
        val raw = prefs.getString(KEY, "[]") ?: "[]"
        val arr = JSONArray(raw)
        val out = JSONArray()
        for (i in 0 until arr.length()) {
            val o = arr.getJSONObject(i)
            if (o.getString("id") != id) out.put(o)
        }
        prefs.edit().putString(KEY, out.toString()).apply()
    }
}
