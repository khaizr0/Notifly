package com.khaizro.notifly

import android.annotation.SuppressLint
import android.app.Service
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.content.Context
import android.content.IntentFilter
import android.os.Build
import android.os.Handler
import android.os.Looper
import com.khaizro.notifly.bluetooth.BluetoothMode
import com.khaizro.notifly.data.BluetoothSessionStore

class BluetoothRecoveryManager(
    private val service: Service,
    private val session: BluetoothSessionStore,
    private val adapter: () -> BluetoothAdapter?,
    private val startReceiver: () -> Unit,
    private val connect: (BluetoothDevice, String) -> Unit,
    private val disconnect: () -> Unit,
) {
    private val handler = Handler(Looper.getMainLooper())
    private val receiver = BluetoothPowerReceiver(::schedule, ::onUnavailable)

    fun register() {
        val filter = IntentFilter(BluetoothAdapter.ACTION_STATE_CHANGED)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            service.registerReceiver(receiver, filter, Context.RECEIVER_NOT_EXPORTED)
        } else {
            service.registerReceiver(receiver, filter)
        }
    }

    fun unregister() {
        handler.removeCallbacksAndMessages(null)
        service.unregisterReceiver(receiver)
    }

    fun schedule() {
        handler.removeCallbacksAndMessages(null)
        handler.postDelayed(::resume, RECONNECT_DELAY_MS)
    }

    fun cancel() {
        handler.removeCallbacksAndMessages(null)
    }

    private fun resume() {
        if (adapter()?.isEnabled != true) return onUnavailable()
        when (session.mode()) {
            BluetoothMode.RECEIVER -> startReceiver()
            BluetoothMode.SENDER -> reconnectLastDevice()
        }
    }

    @SuppressLint("MissingPermission")
    private fun reconnectLastDevice() {
        val address = session.deviceAddress() ?: return
        val device = runCatching { adapter()?.getRemoteDevice(address) }.getOrNull() ?: return
        connect(device, session.deviceName() ?: service.getString(R.string.label_unknown_device))
    }

    private fun onUnavailable() = disconnect()

    companion object {
        private const val RECONNECT_DELAY_MS = 3000L
    }
}
