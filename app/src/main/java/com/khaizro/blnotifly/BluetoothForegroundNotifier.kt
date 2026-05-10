package com.khaizro.blnotifly

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.app.PendingIntent
import android.content.Intent
import android.content.pm.ServiceInfo
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.content.ContextCompat

class BluetoothForegroundNotifier(private val service: Service) {
    fun createChannel() {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) return
        val channel = NotificationChannel(CHANNEL_ID, CHANNEL_NAME, NotificationManager.IMPORTANCE_LOW)
        service.getSystemService(NotificationManager::class.java).createNotificationChannel(channel)
    }

    fun start() {
        val notification = notification()
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
            service.startForeground(
                NOTIFICATION_ID,
                notification,
                ServiceInfo.FOREGROUND_SERVICE_TYPE_CONNECTED_DEVICE,
            )
        } else {
            service.startForeground(NOTIFICATION_ID, notification)
        }
    }

    private fun notification(): Notification {
        val stopIntent = Intent(service, BluetoothService::class.java).apply {
            action = BluetoothService.ACTION_STOP_SERVICE
        }
        val stopPendingIntent = PendingIntent.getService(
            service,
            0,
            stopIntent,
            PendingIntent.FLAG_IMMUTABLE
        )

        return NotificationCompat.Builder(service, CHANNEL_ID)
            .setContentTitle(service.getString(R.string.service_active_title))
            .setContentText(service.getString(R.string.service_active_content))
            .setSmallIcon(R.drawable.ic_notification)
            .setColor(ContextCompat.getColor(service, R.color.md_theme_dark_primary))
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .addAction(R.drawable.ic_notification, service.getString(R.string.btn_stop_service), stopPendingIntent)
            .build()
    }

    companion object {
        private const val CHANNEL_ID = "BluetoothServiceChannel"
        private const val CHANNEL_NAME = "Bluetooth Service Channel"
        private const val NOTIFICATION_ID = 1
    }
}
