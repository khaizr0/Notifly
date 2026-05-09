package com.khaizro.notifly.domain

class NotificationPayload(
    val appName: String,
    val title: String,
    val text: String,
    val isForwarded: Boolean = false
) {
    fun serialize(): String {
        val cleanTitle = title.replace("|", "-").replace("\n", " ")
        val cleanText = text.replace("|", "-").replace("\n", " ")
        val flag = if (isForwarded) "1" else "0"
        return "NOTIF|$appName|$cleanTitle|$cleanText|$flag"
    }

    companion object {
        fun parse(raw: String): NotificationPayload? {
            if (!raw.startsWith("NOTIF|")) return null
            val parts = raw.split("|")
            if (parts.size < 4) return null
            val isForwarded = if (parts.size >= 5) parts[4] == "1" else false
            return NotificationPayload(parts[1], parts[2], parts[3], isForwarded)
        }
    }
}
