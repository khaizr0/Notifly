package com.khaizro.blnotifly.data

import android.content.Context

class AppPreferences(context: Context) {
    private val prefs = context.getSharedPreferences("NotiflyPrefs", Context.MODE_PRIVATE)

    fun allowedApps(): Set<String> {
        val apps = prefs.getStringSet("allowed_apps", null)
        // Nếu là lần đầu hoặc chưa lưu gì, trả về tập hợp rỗng nhưng 
        // ở NotificationService ta sẽ xử lý mặc định nếu cần.
        return apps ?: emptySet()
    }
    
    fun saveAllowedApps(apps: Set<String>) {
        prefs.edit().putStringSet("allowed_apps", apps).apply()
    }
    
    fun isFirstRun(): Boolean = prefs.getBoolean("is_first_run", true)
    
    fun markFirstRunDone() {
        prefs.edit().putBoolean("is_first_run", false).apply()
    }
}
