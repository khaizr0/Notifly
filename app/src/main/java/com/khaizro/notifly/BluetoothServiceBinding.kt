package com.khaizro.notifly

import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.os.IBinder

class BluetoothServiceBinding(
    private val context: Context,
    private val onEvent: (String) -> Unit
) : ServiceConnection {
    var service: BluetoothService? = null
    var isBound = false

    fun bind() {
        context.bindService(
            Intent(context, BluetoothService::class.java),
            this,
            Context.BIND_AUTO_CREATE
        )
    }

    fun unbind() {
        if (isBound) {
            context.unbindService(this)
            isBound = false
        }
    }

    override fun onServiceConnected(name: ComponentName, binder: IBinder) {
        service = (binder as BluetoothService.LocalBinder).getService().apply {
            serviceHandler = onEvent
        }
        isBound = true
    }

    override fun onServiceDisconnected(name: ComponentName) {
        isBound = false
        service = null
    }
}
