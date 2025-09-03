package com.example.drugtestchecker

import android.os.Bundle
import android.webkit.WebView
import android.widget.ArrayAdapter
import android.widget.ListView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import java.io.File

class ViewHtmlActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    val header = TextView(this)
    header.text = "Note: Snapshots are saved HTML only. Images, scripts, or external links may not load."
    header.setPadding(16, 16, 16, 8)

    val list = ListView(this)
    list.addHeaderView(header, null, false)
    setContentView(list)

        val dir = File(filesDir, "html")
        val files = if (dir.exists()) dir.listFiles()?.sortedBy { it.name }?.reversed() ?: emptyList() else emptyList()
        val index = File(dir, "index.txt")
        val titles = mutableListOf<String>()
        val mapping = mutableListOf<File>()
        if (index.exists()) {
            val lines = index.readLines().reversed()
            for (ln in lines) {
                val parts = ln.split("|")
                if (parts.size >= 5) {
                    val name = parts[0]
                    val ts = parts[1]
                    val profileId = parts[2]
                    val profileName = parts[3]
                    val msg = parts.subList(4, parts.size).joinToString("|")
                    val f = File(dir, name)
                    if (f.exists()) {
                        titles.add("$ts\n$profileName — ${msg.take(80)}")
                        mapping.add(f)
                    }
                }
            }
        } else {
            for (f in files) { titles.add(f.name); mapping.add(f) }
        }

        val adapter = ArrayAdapter(this, android.R.layout.simple_list_item_2, android.R.id.text1, titles)
        list.adapter = adapter

        list.setOnItemClickListener { _, _, position, _ ->
            val f = mapping[position]
            val wv = WebView(this)
            setContentView(wv)
            wv.settings.allowFileAccess = true
            wv.settings.javaScriptEnabled = false
            Toast.makeText(this, "Opening snapshot; some images/links may not work.", Toast.LENGTH_SHORT).show()
            wv.loadUrl("file://" + f.absolutePath)
        }
    }

    override fun onBackPressed() {
        // if a WebView is visible, go back to the list
        val root = findViewById<ListView>(android.R.id.list)
        if (root == null) super.onBackPressed() else {
            // fall back to default
            super.onBackPressed()
        }
    }
}
