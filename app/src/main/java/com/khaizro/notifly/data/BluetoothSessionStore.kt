package com.khaizro.notifly.data

import android.content.Context
import com.khaizro.notifly.bluetooth.BluetoothMode

class BluetoothSessionStore(context: Context) {
    private val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    fun mode(): String = prefs.getString(KEY_MODE, BluetoothMode.IDLE) ?: BluetoothMode.IDLE
    fun deviceAddress(): String? = prefs.getString(KEY_ADDRESS, null)
    fun deviceName(): String? = prefs.getString(KEY_NAME, null)

    fun saveReceiver() {
        prefs.edit().putString(KEY_MODE, BluetoothMode.RECEIVER).remove(KEY_ADDRESS).remove(KEY_NAME).apply()
    }

    fun saveSender(address: String, name: String?) {
        prefs.edit()
            .putString(KEY_MODE, BluetoothMode.SENDER)
            .putString(KEY_ADDRESS, address)
            .putString(KEY_NAME, name)
            .apply()
    }

    fun clear() {
        prefs.edit().putString(KEY_MODE, BluetoothMode.IDLE).remove(KEY_ADDRESS).remove(KEY_NAME).apply()
    }

    companion object {
        private const val PREFS_NAME = "NotiflyBluetoothSession"
        private const val KEY_MODE = "mode"
        private const val KEY_ADDRESS = "address"
        private const val KEY_NAME = "name"
    }
}
