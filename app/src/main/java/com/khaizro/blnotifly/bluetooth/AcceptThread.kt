package com.khaizro.blnotifly.bluetooth

import android.annotation.SuppressLint
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothServerSocket
import android.bluetooth.BluetoothSocket
import android.util.Log
import java.io.IOException

class AcceptThread(
    bluetoothAdapter: BluetoothAdapter?,
    private val onConnected: (BluetoothSocket) -> Unit,
    private val onEvent: (String) -> Unit,
) : Thread("NotiflyAcceptThread") {
    @SuppressLint("MissingPermission")
    private val serverSocket: BluetoothServerSocket? = runCatching {
        bluetoothAdapter?.listenUsingRfcommWithServiceRecord(
            BluetoothConstants.SERVICE_NAME,
            BluetoothConstants.SPP_UUID,
        )
    }.onFailure { Log.e(TAG, "Listen failed", it) }.getOrNull()

    @Volatile
    private var isClosed = false

    override fun run() {
        while (!isInterrupted && !isClosed) {
            val socket = try {
                serverSocket?.accept()
            } catch (_: IOException) {
                null
            }
            if (socket == null) {
                if (!isClosed) {
                    isClosed = true
                    onEvent(BluetoothEvent.DISCONNECTED)
                }
                return
            }
            onConnected(socket)
            cancel()
            return
        }
    }

    fun cancel() {
        isClosed = true
        interrupt()
        runCatching { serverSocket?.close() }
    }

    companion object {
        private const val TAG = "AcceptThread"
    }
}
