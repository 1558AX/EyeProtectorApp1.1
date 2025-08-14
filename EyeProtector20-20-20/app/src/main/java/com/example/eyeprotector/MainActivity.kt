package com.example.eyeprotector

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat

class MainActivity : AppCompatActivity() {
    private lateinit var toggleButton: Button
    private var running = false
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        toggleButton = findViewById(R.id.toggleButton)
        toggleButton.setOnClickListener {
            if (running) {
                stopService(Intent(this, TimerService::class.java))
                toggleButton.text = getString(R.string.start)
                running = false
            } else {
                ContextCompat.startForegroundService(this, Intent(this, TimerService::class.java))
                toggleButton.text = getString(R.string.stop)
                running = true
            }
        }
    }
}