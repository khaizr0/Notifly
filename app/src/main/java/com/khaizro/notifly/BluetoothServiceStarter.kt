package com.khaizro.notifly

import android.content.Context
import android.content.Intent
import android.os.Build

object BluetoothServiceStarter {
    fun start(context: Context) {
        val intent = Intent(context, BluetoothService::class.java)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            context.startForegroundService(intent)
        } else {
            context.startService(intent)
        }
    }
}
