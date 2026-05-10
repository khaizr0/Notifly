package com.khaizro.blnotifly.bluetooth

import android.annotation.SuppressLint
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothSocket
import android.util.Log

class ConnectThread(
    private val bluetoothAdapter: BluetoothAdapter?,
    device: BluetoothDevice,
    private val onConnected: (BluetoothSocket) -> Unit,
    private val onEvent: (String) -> Unit,
) : Thread("NotiflyConnectThread") {
    @SuppressLint("MissingPermission")
    private val socket: BluetoothSocket? = runCatching {
        device.createRfcommSocketToServiceRecord(BluetoothConstants.SPP_UUID)
    }.onFailure { Log.e(TAG, "Create socket failed", it) }.getOrNull()

    @Volatile
    private var isClosed = false
    @Volatile
    private var isHandedOver = false

    @SuppressLint("MissingPermission")
    override fun run() {
        bluetoothAdapter?.cancelDiscovery()
        val activeSocket = socket ?: run {
            if (!isClosed) onEvent(BluetoothEvent.DISCONNECTED)
            return
        }
        runCatching {
            activeSocket.connect()
            if (!isClosed) {
                isHandedOver = true
                onConnected(activeSocket)
            } else {
                runCatching { activeSocket.close() }
            }
        }.onFailure {
            if (!isClosed) {
                isClosed = true
                Log.e(TAG, "Connect failed", it)
                runCatching { activeSocket.close() }
                onEvent(BluetoothEvent.DISCONNECTED)
            }
        }
    }

    fun cancel() {
        isClosed = true
        interrupt()
        if (!isHandedOver) {
            runCatching { socket?.close() }
        }
    }

    companion object {
        private const val TAG = "ConnectThread"
    }
}
