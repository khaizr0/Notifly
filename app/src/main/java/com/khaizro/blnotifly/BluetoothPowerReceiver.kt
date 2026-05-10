package com.khaizro.blnotifly

import android.bluetooth.BluetoothAdapter
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent

class BluetoothPowerReceiver(
    private val onAvailable: () -> Unit,
    private val onUnavailable: () -> Unit,
) : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action != BluetoothAdapter.ACTION_STATE_CHANGED) return
        when (intent.getIntExtra(BluetoothAdapter.EXTRA_STATE, BluetoothAdapter.ERROR)) {
            BluetoothAdapter.STATE_ON -> onAvailable()
            BluetoothAdapter.STATE_OFF, BluetoothAdapter.STATE_TURNING_OFF -> onUnavailable()
        }
    }
}
