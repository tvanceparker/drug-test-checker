package com.example.drugtestchecker

import android.app.Activity
import android.content.Context
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
        val names = files.map { it.name }
        val adapter = ArrayAdapter(this, android.R.layout.simple_list_item_1, names)
        list.adapter = adapter

        list.setOnItemClickListener { _, _, position, _ ->
            val f = files[position]
            val wv = WebView(this)
            setContentView(wv)
            // Load from file:// URL
            wv.settings.allowFileAccess = true
            wv.settings.javaScriptEnabled = false
            wv.loadUrl("file://" + f.absolutePath)
        }
    }
}
