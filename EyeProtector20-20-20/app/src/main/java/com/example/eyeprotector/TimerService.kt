package com.example.eyeprotector

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.Handler
import android.os.IBinder
import android.os.Looper
import android.os.PowerManager
import androidx.core.app.NotificationCompat

class TimerService : Service() {

    private val handler = Handler(Looper.getMainLooper())
    private var startTime: Long = 0L
    private val checkIntervalMs = 30_000L
    private val twentyMinutesMs = 20 * 60 * 1000L

    private val foregroundChannelId = "eye_protector_foreground"
    private val alertChannelId = "eye_protector_alerts"

    private val runnable = object : Runnable {
        override fun run() {
            val pm = getSystemService(Context.POWER_SERVICE) as PowerManager
            val screenOn = pm.isInteractive
            if (!screenOn) {
                startTime = System.currentTimeMillis()
            } else {
                val elapsed = System.currentTimeMillis() - startTime
                if (elapsed >= twentyMinutesMs) {
                    showAlertNotification()
                    startTime = System.currentTimeMillis()
                }
            }
            handler.postDelayed(this, checkIntervalMs)
        }
    }

    override fun onCreate() {
        super.onCreate()
        createNotificationChannels()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        startTime = System.currentTimeMillis()
        startForeground(100, buildForegroundNotification())
        handler.post(runnable)
        return START_STICKY
    }

    override fun onDestroy() {
        handler.removeCallbacksAndMessages(null)
        super.onDestroy()
    }

    private fun createNotificationChannels() {
        val nm = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val fg = NotificationChannel(foregroundChannelId, "Service Status", NotificationManager.IMPORTANCE_MIN)
            fg.setShowBadge(false)
            nm.createNotificationChannel(fg)

            val alert = NotificationChannel(alertChannelId, "20-20-20 Alerts", NotificationManager.IMPORTANCE_LOW)
            alert.setShowBadge(false)
            nm.createNotificationChannel(alert)
        }
    }

    private fun buildForegroundNotification(): Notification {
        val intent = Intent(this, MainActivity::class.java)
        val pi = PendingIntent.getActivity(this, 0, intent, PendingIntent.FLAG_IMMUTABLE)
        return NotificationCompat.Builder(this, foregroundChannelId)
            .setSmallIcon(android.R.drawable.ic_dialog_info)
            .setContentTitle(getString(R.string.service_running))
            .setContentText("سيتذكرك كل 20 دقيقة عندما تكون الشاشة قيد التشغيل")
            .setContentIntent(pi)
            .setOngoing(true)
            .setSilent(true)
            .build()
    }

    private fun showAlertNotification() {
        val nm = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        val notification = NotificationCompat.Builder(this, alertChannelId)
            .setSmallIcon(android.R.drawable.ic_dialog_info)
            .setContentTitle(getString(R.string.notify_title))
            .setContentText(getString(R.string.notify_text))
            .setAutoCancel(true)
            .setSilent(true)
            .build()
        nm.notify(1, notification)
    }

    override fun onBind(intent: Intent?): IBinder? = null
}