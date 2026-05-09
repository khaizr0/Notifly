package com.khaizro.notifly

import android.content.Intent
import android.service.notification.NotificationListenerService
import android.service.notification.StatusBarNotification
import android.util.Log
import com.khaizro.notifly.data.AppPreferences
import com.khaizro.notifly.domain.NotificationPayload

class NotificationService : NotificationListenerService() {

    override fun onListenerConnected() {
        super.onListenerConnected()
        Log.i("NotiflyNS", "NotificationListenerService Connected and Bound!")
    }

    override fun onListenerDisconnected() {
        super.onListenerDisconnected()
        Log.w("NotiflyNS", "NotificationListenerService Disconnected!")
    }
    
    override fun onNotificationPosted(sbn: StatusBarNotification?) {
        super.onNotificationPosted(sbn)
        val s = sbn ?: return
        val pkg = s.packageName ?: return
        
        // 1. Check if this is a notification we already handled (to prevent loop)
        val extras = s.notification.extras
        val isForwarded = extras?.getBoolean("is_forwarded_by_notifly", false) ?: false
        if (isForwarded) {
            Log.v("NotiflyNS", "Ignoring notification already forwarded by us.")
            return
        }

        // 2. Handle self-test messages specially
        if (pkg == packageName) {
            val title = extras.getCharSequence("android.title")?.toString() ?: "No Title"
            val text = extras.getCharSequence("android.text")?.toString() ?: "No Content"
            if (title == "Test Message" || text.contains("Hello from Bluetooth")) {
                Log.d("NotiflyNS", "Internal test notification detected, forwarding.")
            } else {
                return
            }
        }
        
        val prefs = AppPreferences(this)
        val allowedApps = prefs.allowedApps()
        val firstRun = prefs.isFirstRun()
        
        Log.d("NotiflyNS", "onNotificationPosted: $pkg")
        val allowed = firstRun || allowedApps.contains(pkg) || pkg == packageName
        Log.d("NotiflyNS", "Verdict for $pkg: allowed=$allowed")

        if (!allowed) return

        val appLabel = runCatching {
            packageManager.getApplicationLabel(packageManager.getApplicationInfo(pkg, 0)).toString()
        }.getOrDefault(pkg)

        val title = extras.getCharSequence("android.title")?.toString() ?: "No Title"
        val text = extras.getCharSequence("android.text")?.toString() ?: 
                  extras.getCharSequence("android.bigText")?.toString() ?: "No Content"
        
        val payload = NotificationPayload(
            appName = appLabel,
            title = title,
            text = text,
            isForwarded = true
        )
        
        Log.i("NotiflyNS", "Broadcasting TRANSFER for: $appLabel ($pkg)")
        sendBroadcast(Intent("com.khaizro.notifly.TRANSFER").apply {
            putExtra("data", payload.serialize())
            setPackage(packageName)
        })
    }
}
