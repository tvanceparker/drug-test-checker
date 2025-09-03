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
import org.jsoup.Jsoup
import java.time.*
import android.widget.TextView
import android.provider.Settings
import android.os.Build
import java.io.File
import android.app.Activity
import androidx.core.app.ActivityCompat
import android.content.pm.PackageManager

class MainActivity : AppCompatActivity() {
    private lateinit var etName: EditText
    private lateinit var etPin: EditText
    private lateinit var etLast: EditText
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // Request notification permission on Android 13+
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (checkSelfPermission(android.Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
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
    val btnImmediate = findViewById<Button>(R.id.btnImmediateTest)
    val tvLast = findViewById<TextView>(R.id.tvLastRun)
    val tvStatus = findViewById<TextView>(R.id.tvStatusCard)

        fun showProfile(p: Profile) {
            etName.setText(p.name)
            etPin.setText(p.pin)
            etLast.setText(p.last4)
            etName.isEnabled = false
            etPin.isEnabled = false
            etLast.isEnabled = false
        }

        // show last run if logs exist
        Thread {
            try {
                val f = File(filesDir, "drug_test_logs.csv")
                if (f.exists()) {
                    val lines = f.readLines()
                    val last = if (lines.size > 1) lines.last() else null
                    runOnUiThread {
                        tvLast.text = "Last run: ${last ?: "--"}"
                        if (last != null) {
                            val parts = last.split(",", limit = 4)
                            val message = parts.getOrNull(3)?.trim('"') ?: "--"
                            tvStatus.text = "Status: ${message}"
                        }
                    }
                }
            } catch (e: Exception) { }
        }.start()

        btnSchedule.setOnClickListener {
            val mgr = AlarmScheduler(this)
            // check exact alarm permission on Android 12+
            if (!ExactAlarmHelper.hasExactAlarmPermission(this)) {
                // Guide the user to the exact alarm settings rather than attempting to schedule
                ExactAlarmHelper.openExactAlarmSettings(this)
                Toast.makeText(this, "Exact alarm permission is required for a guaranteed 03:10 alarm. Please enable it in settings.", Toast.LENGTH_LONG).show()
            } else {
                mgr.scheduleDailyAtBoise(3, 10)
                Toast.makeText(this, "Scheduled daily alarm at 03:10 Boise", Toast.LENGTH_LONG).show()
            }
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

        btnImmediate.setOnClickListener {
            // schedule test in 1 minute for debugging
            val mgr = AlarmScheduler(this)
            mgr.scheduleInMinutes(1)
            Toast.makeText(this, "Immediate test scheduled in 1 minute", Toast.LENGTH_SHORT).show()
        }

        btnViewLogs.setOnClickListener {
            val i = android.content.Intent(this, ViewLogsActivity::class.java)
            startActivity(i)
        }

        btnViewHtml.setOnClickListener {
            startActivity(Intent(this, ViewHtmlActivity::class.java))
        }

        // ensure there is an active profile; if not, force add
        val prefs = getSharedPreferences("dtc", Context.MODE_PRIVATE)
        val active = prefs.getString("active_profile", null)
        val profiles = ProfileHelper.getProfiles(this)
        if (active == null || profiles.isEmpty()) {
            // force add profile
            startActivityForResult(Intent(this, AddProfileActivity::class.java), 502)
        } else {
            val p = profiles.find { it.id == active } ?: profiles.first()
            showProfile(p)
            // ensure active credentials saved
            prefs.edit().putString("pin", p.pin).putString("last4", p.last4).apply()
        }
    }

    fun runCheck(pin: String, last4: String): String {
        try {
            val doc = Jsoup.connect("https://drugtestcheck.com/")
                .userAgent("Mozilla/5.0 (Android)")
                .timeout(10000)
                .data("callInCode", pin, "lastName", last4)
                .post()

            val labels = doc.select("label").map { it.text() }
            for (t in labels) {
                if (t.contains("You are required to test today", true)) return t
                if (t.contains("You are not required to test today", true)) return t
                if (t.contains("Please try again during your agency's call-in timeframe", true)) return t
            }

            // attach labels for debugging when no match
            return "No recognizable response: ${labels.joinToString(" | ")}" 
        } catch (e: Exception) {
            return "Error: ${e.message}"
        }
    }

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
