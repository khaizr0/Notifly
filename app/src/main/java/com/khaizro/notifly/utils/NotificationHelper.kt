package com.khaizro.notifly.utils

import android.Manifest
import android.annotation.SuppressLint
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.util.Log
import androidx.core.app.ActivityCompat
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import com.khaizro.notifly.MainActivity
import com.khaizro.notifly.NotificationService
import com.khaizro.notifly.domain.NotificationPayload

object NotificationHelper {

    private const val CHANNEL_ID = "notifly_channel_v1"
    private const val CHANNEL_NAME = "Notifly Notifications"

    fun createNotificationChannel(context: Context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(CHANNEL_ID, CHANNEL_NAME, NotificationManager.IMPORTANCE_HIGH).apply {
                description = "Forwarded notifications from other devices"
            }
            val manager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            manager.createNotificationChannel(channel)
        }
    }

    /**
     * Toggles the NotificationListenerService to force Android to re-bind it.
     * This is sometimes necessary if the service doesn't start automatically.
     */
    fun toggleListenerService(context: Context) {
        val componentName = ComponentName(context, NotificationService::class.java)
        context.packageManager.setComponentEnabledSetting(
            componentName,
            PackageManager.COMPONENT_ENABLED_STATE_DISABLED,
            PackageManager.DONT_KILL_APP
        )
        context.packageManager.setComponentEnabledSetting(
            componentName,
            PackageManager.COMPONENT_ENABLED_STATE_ENABLED,
            PackageManager.DONT_KILL_APP
        )
        Log.i("NotiflyNH", "NotificationService toggled to force re-bind")
    }

    @SuppressLint("MissingPermission")
    fun showLocalNotification(context: Context, payload: NotificationPayload) {
        Log.d("NotiflyNH", "showLocalNotification: ${payload.appName} - ${payload.title}")
        if (!canPostNotifications(context)) {
            Log.w("NotiflyNH", "Missing POST_NOTIFICATIONS permission")
            return
        }
        try {
            val notification = buildNotification(context, payload).build()
            // Use a stable ID based on content hash or a fixed range to avoid piling up thousands of notifications
            // but keep them distinct enough.
            val id = (payload.appName + payload.title).hashCode()
            NotificationManagerCompat.from(context).notify(
                id,
                notification,
            )
            Log.d("NotiflyNH", "Notification posted successfully with ID: $id")
        } catch (e: Exception) {
            Log.e("NotiflyNH", "Error posting notification", e)
        }
    }

    private fun buildNotification(context: Context, payload: NotificationPayload): NotificationCompat.Builder {
        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }
        val pendingIntent = PendingIntent.getActivity(
            context,
            System.currentTimeMillis().toInt(),
            intent,
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT,
        )
        val extras = Bundle().apply {
            putBoolean("is_forwarded_by_notifly", true)
        }
        return NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(com.khaizro.notifly.R.drawable.ic_notification)
            .setColor(ContextCompat.getColor(context, com.khaizro.notifly.R.color.md_theme_dark_primary))
            .setContentTitle("${payload.appName}: ${payload.title}")
            .setContentText(payload.text)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setDefaults(NotificationCompat.DEFAULT_ALL)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)
            .setOngoing(false) // Explicitly set to false to ensure it's dismissable
            .addExtras(extras)
    }

    private fun canPostNotifications(context: Context): Boolean {
        return Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU ||
            ActivityCompat.checkSelfPermission(
                context,
                Manifest.permission.POST_NOTIFICATIONS,
            ) == PackageManager.PERMISSION_GRANTED
    }
}
