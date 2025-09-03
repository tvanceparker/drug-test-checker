package com.example.drugtestchecker

import android.os.Bundle
import android.widget.LinearLayout
import android.widget.ScrollView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import java.io.File
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter
import java.time.format.DateTimeParseException

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
        val inFmtIso = DateTimeFormatter.ISO_ZONED_DATE_TIME
        val outFmt = DateTimeFormatter.ofPattern("EEE, MMM d yyyy • h:mm a z")
        // skip header
        for (i in 1 until lines.size) {
            val line = lines[i]
            val parts = line.split(",", limit = 4)
            val tv = TextView(this)
            val tsRaw = parts.getOrNull(0) ?: "-"
            val pin = parts.getOrNull(1) ?: "-"
            val last4 = parts.getOrNull(2) ?: "-"
            val message = parts.getOrNull(3) ?: "-"
            val tsPretty = try {
                ZonedDateTime.parse(tsRaw, inFmtIso).format(outFmt)
            } catch (e: DateTimeParseException) { tsRaw }
            val pretty = "${tsPretty}\nPIN/Last4: ${pin}/${last4}\n${message}"
            tv.text = pretty
            tv.setPadding(16,16,16,16)
            container.addView(tv)
        }
    }
}
