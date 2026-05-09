package com.khaizro.notifly.bluetooth

import android.annotation.SuppressLint
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothSocket
import android.util.Log
import java.io.IOException

class BluetoothConnectionManager(
    private val adapter: BluetoothAdapter?,
    private val onEvent: ((String) -> Unit)?,
) {
    private var acceptThread: AcceptThread? = null
    private var connectThread: ConnectThread? = null
    private var connectedThread: ConnectedThread? = null
    private var sessionId = 0

    fun startServer() {
        runCatching {
            val id = nextSession()
            Log.d("BtConnMgr", "startServer session: $id")
            cancelThreads()
            acceptThread = AcceptThread(adapter, { onConnected(id, it) }) { emit(id, it) }
            acceptThread?.start()
        }.onFailure { emit(BluetoothEvent.errorEvent(it.message)) }
    }

    fun connectToDevice(device: BluetoothDevice) {
        runCatching {
            val id = nextSession()
            Log.d("BtConnMgr", "connectToDevice session: $id, device: ${device.address}")
            cancelThreads()
            connectThread = ConnectThread(adapter, device, { onConnected(id, it) }) { emit(id, it) }
            connectThread?.start()
        }.onFailure { emit(BluetoothEvent.errorEvent(it.message)) }
    }

    fun sendData(data: String) {
        connectedThread?.write(data.toByteArray())
            ?: Log.w("BtConnMgr", "No active connection to send data.")
    }

    fun cancelAll() {
        Log.d("BtConnMgr", "cancelAll")
        nextSession()
        cancelThreads()
    }

    private fun cancelThreads() {
        acceptThread?.cancel()
        connectThread?.cancel()
        connectedThread?.cancel()
        acceptThread = null
        connectThread = null
        connectedThread = null
    }

    @SuppressLint("MissingPermission")
    private fun onConnected(id: Int, socket: BluetoothSocket) {
        Log.d("BtConnMgr", "onConnected session: $id, activeSession: $sessionId")
        if (!isActive(id)) {
            Log.w("BtConnMgr", "onConnected called for inactive session, closing socket")
            runCatching { socket.close() }
            return
        }
        
        cancelThreads()
        
        Log.d("BtConnMgr", "Creating ConnectedThread")
        connectedThread = ConnectedThread(socket) { emit(id, it) }
        connectedThread?.start()
        emit(id, BluetoothEvent.connectedEvent(socket.remoteDevice?.name ?: "Unknown Device"))
    }

    private fun emit(event: String) = onEvent?.invoke(event)

    private fun emit(id: Int, event: String) {
        if (isActive(id)) emit(event)
        else Log.v("BtConnMgr", "Ignoring event for old session $id: $event")
    }

    private fun nextSession(): Int = ++sessionId

    private fun isActive(id: Int): Boolean = id == sessionId
}
