package com.example.drugtestchecker

import android.os.Bundle
import android.webkit.WebView
import android.widget.ArrayAdapter
import android.widget.ListView
import androidx.appcompat.app.AppCompatActivity
import java.io.File

class ViewHtmlActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val list = ListView(this)
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
                if (parts.size >= 4) {
                    val name = parts[0]
                    val ts = parts[1]
                    val profile = parts[2]
                    val msg = parts.subList(3, parts.size).joinToString("|")
                    val f = File(dir, name)
                    if (f.exists()) {
                        titles.add("$ts — $profile — ${msg.take(40)}")
                        mapping.add(f)
                    }
                }
            }
        } else {
            for (f in files) { titles.add(f.name); mapping.add(f) }
        }

        val adapter = ArrayAdapter(this, android.R.layout.simple_list_item_1, titles)
        list.adapter = adapter

        list.setOnItemClickListener { _, _, position, _ ->
            val f = mapping[position]
            val wv = WebView(this)
            setContentView(wv)
            wv.settings.allowFileAccess = true
            wv.settings.javaScriptEnabled = false
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
