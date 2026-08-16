package com.whatsThatLink.app

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import android.service.notification.NotificationListenerService
import android.service.notification.StatusBarNotification
import android.util.Log
import androidx.core.app.NotificationCompat
import com.whatsThatLink.app.data.PhishingRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch

class WhatsThatLinkNotificationListener : NotificationListenerService() {

    private val job = SupervisorJob()
    private val scope = CoroutineScope(Dispatchers.IO + job)
    private lateinit var repository: PhishingRepository

    companion object {
        private const val TAG = "WhatsThatLink"
        private const val CHANNEL_ID = "phishing_alerts"
        private const val CHANNEL_NAME = "Phishing Alerts"
    }

    override fun onCreate() {
        super.onCreate()
        repository = PhishingRepository(this)
        createNotificationChannel()
        Log.d(TAG, "Notification Listener Service Created")
    }

    override fun onListenerConnected() {
        super.onListenerConnected()
        Log.d(TAG, "Notification Listener Connected")
    }

    override fun onDestroy() {
        super.onDestroy()
        job.cancel()
    }

    override fun onNotificationPosted(sbn: StatusBarNotification?) {
        if (sbn == null) return

        if (sbn.packageName != "com.whatsapp") {
            return
        }

        val extras = sbn.notification.extras
        val title = extras.getString("android.title") ?: ""
        val text = extras.getCharSequence("android.text")?.toString() ?: ""
        val notificationText = "$title $text"

        val urlPattern = Regex("""https?://[^\s]+""")
        val match = urlPattern.find(notificationText)

        if (match != null) {
            val detectedUrl = match.value.trimEnd('.', ',', '!', '?', ')', ']')
            Log.d(TAG, "URL detected in WhatsApp")
            performScan(detectedUrl)
        }
    }

    private fun performScan(url: String) {
        scope.launch {
            val result = repository.scanUrl(url)
            result.onSuccess { scan ->
                Log.d(TAG, "Scan successful: ${scan.risk}")
                showResultNotification(scan.risk, scan.id)
            }.onFailure { e ->
                Log.e(TAG, "Scan failed: ${e.message}")
            }
        }
    }

    private fun showResultNotification(risk: String, scanId: Long) {
        val intent = Intent(this, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            putExtra("EXTRA_SCAN_ID", scanId)
        }

        val pendingIntent = PendingIntent.getActivity(
            this, scanId.toInt(), intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val title = "Link Analysis: $risk Risk"
        val content = "A link was analyzed. Tap to see the full report."

        val builder = NotificationCompat.Builder(this, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_scan_notification)
            .setContentTitle(title)
            .setContentText(content)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setDefaults(NotificationCompat.DEFAULT_ALL)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)
            .setVisibility(NotificationCompat.VISIBILITY_PRIVATE)

        val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.notify(scanId.toInt(), builder.build())
        Log.d(TAG, "Notification posted for scan ID: $scanId")
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val importance = NotificationManager.IMPORTANCE_HIGH
            val channel = NotificationChannel(CHANNEL_ID, CHANNEL_NAME, importance).apply {
                description = "Notifications for phishing link analysis results"
                enableLights(true)
                lightColor = android.graphics.Color.RED
                enableVibration(true)
                setShowBadge(true)
                lockscreenVisibility = android.app.Notification.VISIBILITY_PRIVATE
            }
            val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
        }
    }
}
