package com.example.drugtestchecker

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import java.time.*
import android.widget.TextView
import android.provider.Settings
import android.os.Build
import java.io.File
import android.app.Activity
import androidx.core.app.ActivityCompat
import android.content.pm.PackageManager
import android.app.TimePickerDialog
import java.time.format.DateTimeFormatter
import java.time.format.DateTimeParseException

class MainActivity : AppCompatActivity() {
    private lateinit var etName: EditText
    private lateinit var etPin: EditText
    private lateinit var etLast: EditText
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // Proactively prompt for exact alarm and notifications permissions on open
        if (!ExactAlarmHelper.hasExactAlarmPermission(this)) {
            ExactAlarmHelper.openExactAlarmSettings(this)
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (checkSelfPermission(android.Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
                Toast.makeText(this, "Enable notifications so you won't miss alerts.", Toast.LENGTH_LONG).show()
                ActivityCompat.requestPermissions(this, arrayOf(android.Manifest.permission.POST_NOTIFICATIONS), 601)
            }
        }

    val btnSchedule = findViewById<Button>(R.id.btnSchedule)
    val btnEdit = findViewById<Button>(R.id.btnEditProfile)
    val btnAdd = findViewById<Button>(R.id.btnAddProfile)
    etPin = findViewById<EditText>(R.id.etPin)
    etLast = findViewById<EditText>(R.id.etLast)
    etName = findViewById<EditText>(R.id.etName)
    val btnViewLogs = findViewById<Button>(R.id.btnViewLogs)
    val btnViewHtml = findViewById<Button>(R.id.btnViewHtml)
    val tvLast = findViewById<TextView>(R.id.tvLastRun)
    val tvStatus = findViewById<TextView>(R.id.tvStatusCard)
    val tvScheduled = findViewById<TextView>(R.id.tvScheduledTime)
    val prefs = getSharedPreferences("dtc", Context.MODE_PRIVATE)
    val schedHour = prefs.getInt("schedule_hour", 3)
    val schedMin = prefs.getInt("schedule_minute", 10)
    val schedTimeStr = String.format("Scheduled daily check: %02d:%02d (device time)", schedHour, schedMin)
    tvLast.text = "Last run: --\n$schedTimeStr"
    tvScheduled.text = schedTimeStr

        fun showProfile(p: Profile) {
            etName.setText(p.name)
            etPin.setText(p.pin)
            etLast.setText(p.last4)
            etName.isEnabled = false
            etPin.isEnabled = false
            etLast.isEnabled = false
        }

        // show last run if logs exist, with human-friendly timestamp
        Thread {
            try {
                val f = File(filesDir, "drug_test_logs.csv")
                if (f.exists()) {
                    val lines = f.readLines()
                    val last = if (lines.size > 1) lines.last() else null
                    runOnUiThread {
                        if (last != null) {
                            val parts = last.split(",", limit = 4)
                            val tsRaw = parts.getOrNull(0) ?: "--"
                            val inFmt = DateTimeFormatter.ISO_ZONED_DATE_TIME
                            val outFmt = DateTimeFormatter.ofPattern("EEE, MMM d yyyy • h:mm a z")
                            val tsPretty = try { ZonedDateTime.parse(tsRaw, inFmt).format(outFmt) } catch (e: DateTimeParseException) { tsRaw }
                            tvLast.text = "Last run: $tsPretty\n$schedTimeStr"
                        }
                        if (last != null) {
                            val parts = last.split(",", limit = 4)
                            val message = parts.getOrNull(3)?.trim('"') ?: "--"
                            tvStatus.text = message
                        }
                    }
                }
            } catch (e: Exception) { }
        }.start()

        btnSchedule.setOnClickListener {
            val dialog = TimePickerDialog(this, { _, hourOfDay, minute ->
                val sp = getSharedPreferences("dtc", Context.MODE_PRIVATE)
                sp.edit().putInt("schedule_hour", hourOfDay).putInt("schedule_minute", minute).apply()
                val mgr = AlarmScheduler(this)
                if (!ExactAlarmHelper.hasExactAlarmPermission(this)) {
                    ExactAlarmHelper.openExactAlarmSettings(this)
                    Toast.makeText(this, "Enable exact alarms so checks run reliably.", Toast.LENGTH_LONG).show()
                }
                mgr.scheduleDailyAtLocal(hourOfDay, minute)
                val newTime = String.format("%02d:%02d", hourOfDay, minute)
                val newStr = "Scheduled daily check: $newTime (device time)"
                tvLast.text = "Last run: --\n$newStr"
                tvScheduled.text = newStr
                Toast.makeText(this, "Daily check set for $newTime (device time)", Toast.LENGTH_SHORT).show()
            }, schedHour, schedMin, true)
            dialog.show()
        }

        btnEdit.setOnClickListener {
            val i = Intent(this, ProfilesActivity::class.java)
            startActivityForResult(i, 501)
        }

        btnAdd.setOnClickListener {
            val i = Intent(this, AddProfileActivity::class.java)
            startActivityForResult(i, 502)
        }

    // edit/select accessible via Edit Profile button

        btnViewLogs.setOnClickListener {
            val i = android.content.Intent(this, ViewLogsActivity::class.java)
            startActivity(i)
        }

        btnViewHtml.setOnClickListener {
            startActivity(Intent(this, ViewHtmlActivity::class.java))
        }

        // ensure there is an active profile; if not, force add
        val prefs2 = getSharedPreferences("dtc", Context.MODE_PRIVATE)
        val active = prefs2.getString("active_profile", null)
        val profiles = ProfileHelper.getProfiles(this)
        if (active == null || profiles.isEmpty()) {
            // force add profile
            startActivityForResult(Intent(this, AddProfileActivity::class.java), 502)
        } else {
            val p = profiles.find { it.id == active } ?: profiles.first()
            showProfile(p)
            // ensure active credentials saved
            prefs2.edit().putString("pin", p.pin).putString("last4", p.last4).apply()
        }

        // Always (re)schedule the daily check at the saved time on app open
        AlarmScheduler(this).scheduleDailyAtLocal(schedHour, schedMin)
    }

    // ... no direct network calls from MainActivity

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == 501 && resultCode == Activity.RESULT_OK && data != null) {
            // profile selected from ProfilesActivity
            val id = data.getStringExtra("id") ?: return
            val name = data.getStringExtra("name") ?: return
            val pin = data.getStringExtra("pin") ?: return
            val last = data.getStringExtra("last4") ?: return
            // set active
            val prefs = getSharedPreferences("dtc", Context.MODE_PRIVATE)
            prefs.edit().putString("active_profile", id).putString("pin", pin).putString("last4", last).apply()
            etName.setText(name)
            etPin.setText(pin)
            etLast.setText(last)
            etName.isEnabled = false
            etPin.isEnabled = false
            etLast.isEnabled = false
        }

        if (requestCode == 502 && resultCode == Activity.RESULT_OK) {
            // new profile created; read active and show
            val prefs = getSharedPreferences("dtc", Context.MODE_PRIVATE)
            val activeId = prefs.getString("active_profile", null) ?: return
            val p = ProfileHelper.getProfiles(this).find { it.id == activeId } ?: return
            etName.setText(p.name)
            etPin.setText(p.pin)
            etLast.setText(p.last4)
            etName.isEnabled = false
            etPin.isEnabled = false
            etLast.isEnabled = false
        }
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == 601) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Toast.makeText(this, "Notifications enabled", Toast.LENGTH_SHORT).show()
            } else {
                Toast.makeText(this, "Notifications permission denied; you may miss alerts.", Toast.LENGTH_LONG).show()
            }
        }
    }
}
