package com.example.drugtestchecker

import android.os.Bundle
import android.widget.LinearLayout
import android.widget.ScrollView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import java.io.File
import java.time.ZoneId
import java.time.ZonedDateTime

class ViewLogsActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val scroll = ScrollView(this)
        val container = LinearLayout(this)
        container.orientation = LinearLayout.VERTICAL
        scroll.addView(container)

        setContentView(scroll)

        val f = File(filesDir, "drug_test_logs.csv")
        if (!f.exists()) {
            val tv = TextView(this)
            tv.text = "No logs found"
            container.addView(tv)
            return
        }

        val lines = f.readLines()
        // skip header
        for (i in 1 until lines.size) {
            val line = lines[i]
            val parts = line.split(",", limit = 4)
            val tv = TextView(this)
            val timestamp = parts.getOrNull(0) ?: "-"
            val pin = parts.getOrNull(1) ?: "-"
            val last4 = parts.getOrNull(2) ?: "-"
            val message = parts.getOrNull(3) ?: "-"
            val pretty = "Time: ${timestamp}\nCreds: ${pin} / ${last4}\nResult: ${message}\n---"
            tv.text = pretty
            tv.setPadding(12,12,12,12)
            container.addView(tv)
        }
    }
}
