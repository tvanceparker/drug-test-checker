package com.example.drugtestchecker

import android.app.Activity
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import androidx.appcompat.app.AppCompatActivity

class AddProfileActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val etName = EditText(this)
        etName.hint = "Profile name"
        val etPin = EditText(this)
        etPin.hint = "PIN"
        etPin.inputType = android.text.InputType.TYPE_CLASS_NUMBER
        val etLast = EditText(this)
        etLast.hint = "Last4"
        val btn = Button(this)
        btn.text = "Save"

        val layout = android.widget.LinearLayout(this)
        layout.orientation = android.widget.LinearLayout.VERTICAL
        layout.setPadding(16,16,16,16)
        layout.addView(etName)
        layout.addView(etPin)
        layout.addView(etLast)
        layout.addView(btn)
        setContentView(layout)

        // prefill if passed
        val name = intent.getStringExtra("name")
        val pin = intent.getStringExtra("pin")
        val last = intent.getStringExtra("last4")
        if (name != null) etName.setText(name)
        if (pin != null) etPin.setText(pin)
        if (last != null) etLast.setText(last)

        btn.setOnClickListener {
            val n = etName.text.toString()
            val p = etPin.text.toString()
            val l = etLast.text.toString()
            if (n.isBlank() || !p.matches(Regex("\\d{7}")) || !l.matches(Regex("[A-Za-z]{4}"))) {
                android.widget.Toast.makeText(this, "Invalid profile: name, 7-digit PIN, 4-letter Last4 required", android.widget.Toast.LENGTH_LONG).show()
                return@setOnClickListener
            }
            val prof = ProfileHelper.addNewProfile(this, n, p, l)
            // save active
            val prefs = getSharedPreferences("dtc", Activity.MODE_PRIVATE)
            prefs.edit().putString("active_profile", prof.id).putString("pin", prof.pin).putString("last4", prof.last4).apply()
            setResult(Activity.RESULT_OK)
            finish()
        }
    }
}
