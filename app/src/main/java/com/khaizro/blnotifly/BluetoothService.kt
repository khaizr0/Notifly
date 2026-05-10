/*
 * Copyright 2026 khaizr0
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.khaizro.blnotifly
import android.annotation.SuppressLint
import android.app.Service
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothManager
import android.content.*
import android.os.Binder
import android.os.IBinder
import android.util.Log
import androidx.core.content.ContextCompat
import com.khaizro.blnotifly.bluetooth.BluetoothConnectionManager
import com.khaizro.blnotifly.bluetooth.BluetoothEvent
import com.khaizro.blnotifly.data.BluetoothSessionStore

class BluetoothService : Service() {
    private val binder = LocalBinder()
    private val notifier by lazy { BluetoothForegroundNotifier(this) }
    private val session by lazy { BluetoothSessionStore(this) }
    private val manager by lazy { BluetoothConnectionManager(getBluetoothAdapter(), ::onBluetoothEvent) }
    private val recovery by lazy {
        BluetoothRecoveryManager(this, session, ::getBluetoothAdapter, ::startServer, ::connect, ::onBluetoothUnavailable)
    }
    private var currentEvent: String = BluetoothEvent.DISCONNECTED

    var serviceHandler: ((String) -> Unit)? = null
        set(value) { field = value; emitCurrentState() }

    private val transferReceiver = object : BroadcastReceiver() {
        override fun onReceive(c: Context?, i: Intent?) {
            val data = i?.getStringExtra("data") ?: return
            Log.d("BLNotiflyBS", "transferReceiver received data: $data")
            sendData(data)
        }
    }

    inner class LocalBinder : Binder() { fun getService(): BluetoothService = this@BluetoothService }
    override fun onBind(intent: Intent): IBinder = binder

    override fun onCreate() {
        super.onCreate()
        notifier.createChannel()
        recovery.register()
        val filter = IntentFilter("com.khaizro.blnotifly.TRANSFER")
        ContextCompat.registerReceiver(this, transferReceiver, filter, ContextCompat.RECEIVER_NOT_EXPORTED)
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        if (intent?.action == ACTION_STOP_SERVICE) {
            stopSelf()
            return START_NOT_STICKY
        }
        runCatching { notifier.start() }.onFailure { Log.e("BTService", "Start failed", it) }
        return START_STICKY
    }

    fun startServer() { session.saveReceiver(); manager.startServer() }

    @SuppressLint("MissingPermission")
    fun connectToDevice(device: BluetoothDevice) {
        val name = device.name ?: getString(R.string.label_unknown_device)
        session.saveSender(device.address, name)
        connect(device, name)
    }

    fun sendData(data: String) {
        Log.d("BLNotiflyBS", "Sending data to Bluetooth: $data")
        manager.sendData(data)
    }

    fun forgetDevice() { recovery.cancel(); session.clear(); onBluetoothUnavailable() }

    override fun onDestroy() {
        unregisterReceiver(transferReceiver)
        recovery.unregister()
        manager.cancelAll()
        super.onDestroy()
    }

    private fun onBluetoothEvent(event: String) {
        currentEvent = event
        emitCurrentState()
    }

    private fun emitCurrentState() = serviceHandler?.invoke(currentEvent)

    private fun connect(device: BluetoothDevice, name: String) {
        currentEvent = BluetoothEvent.connecting(name)
        emitCurrentState()
        manager.connectToDevice(device)
    }

    private fun onBluetoothUnavailable() { manager.cancelAll(); onBluetoothEvent(BluetoothEvent.DISCONNECTED) }

    private fun getBluetoothAdapter(): BluetoothAdapter? = (getSystemService(BLUETOOTH_SERVICE) as BluetoothManager).adapter

    companion object {
        const val ACTION_STOP_SERVICE = "com.khaizro.blnotifly.STOP_SERVICE"
    }
}
