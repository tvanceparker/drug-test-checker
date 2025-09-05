package com.example.drugtestchecker

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import android.widget.TextView
import java.io.File

class DebugLogActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val tv = TextView(this)
        tv.setPadding(16,16,16,16)
        tv.textSize = 12f
        setContentView(tv)

        try {
            val dbg = File(filesDir, "dtc_debug.txt")
            val logs = File(filesDir, "drug_test_logs.csv")
            val sb = StringBuilder()
            sb.append("--- dtc_debug.txt ---\n")
            if (dbg.exists()) sb.append(dbg.readText()) else sb.append("(no debug file)\n")
            sb.append("\n--- drug_test_logs.csv (tail 200 lines) ---\n")
            if (logs.exists()) {
                val lines = logs.readLines()
                val tail = lines.takeLast(200)
                tail.forEach { sb.append(it).append('\n') }
            } else sb.append("(no logs)\n")
            tv.text = sb.toString()
        } catch (e: Exception) {
            tv.text = "Failed to read debug logs: ${e.message}"
        }
    }
}
