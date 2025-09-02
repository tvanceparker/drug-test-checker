package com.example.drugtestchecker

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.ListView
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity

class ProfilesActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_profiles)

        val listView = findViewById<ListView>(R.id.listProfiles)
        val btnClose = findViewById<Button>(R.id.btnClose)

        fun load() {
            val profiles = ProfileHelper.getProfiles(this)
            val items = profiles.map { "${it.name} — ${it.pin} — ${it.last4}" }
            listView.adapter = ArrayAdapter(this, android.R.layout.simple_list_item_1, items)
            listView.setOnItemClickListener { _, _, pos, _ ->
                val p = profiles[pos]
                val i = Intent()
                i.putExtra("id", p.id)
                i.putExtra("name", p.name)
                i.putExtra("pin", p.pin)
                i.putExtra("last4", p.last4)
                setResult(Activity.RESULT_OK, i)
                finish()
            }
            listView.setOnItemLongClickListener { _, _, pos, _ ->
                val p = profiles[pos]
                AlertDialog.Builder(this).setTitle("Delete").setMessage("Delete profile ${p.name}?")
                    .setPositiveButton("Delete") { _, _ -> ProfileHelper.deleteProfile(this, p.id); load() }
                    .setNegativeButton("Cancel", null).show()
                true
            }
        }

        btnClose.setOnClickListener { finish() }
        load()
    }
}
