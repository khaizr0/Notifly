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

package com.khaizro.notifly
import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import com.khaizro.notifly.bluetooth.BluetoothEvent
import com.khaizro.notifly.domain.NotificationPayload
import com.khaizro.notifly.utils.BluetoothUIHelper
import com.khaizro.notifly.utils.MainActivityUIHelper
import com.khaizro.notifly.utils.NotificationHelper
import com.khaizro.notifly.utils.PermissionManager

class MainActivity : AppCompatActivity() {
    private lateinit var ui: MainActivityUIHelper
    private lateinit var serviceBinding: BluetoothServiceBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_main)
        ui = MainActivityUIHelper(this).also { it.setupViews() }
        serviceBinding = BluetoothServiceBinding(this) { runOnUiThread { handleEvent(it) } }.also { it.bind() }
        NotificationHelper.createNotificationChannel(this)
        NotificationHelper.toggleListenerService(this)
        setupClickListeners()
    }

    override fun onResume() {
        super.onResume()
        ui.updatePermissionState()
    }

    override fun onDestroy() {
        serviceBinding.unbind()
        super.onDestroy()
    }

    private fun setupClickListeners() = with(ui) {
        btnPermission.setOnClickListener { PermissionManager.requestPermissions(this@MainActivity) }
        btnNotificationAccess.setOnClickListener { PermissionManager.openNotificationAccessSettings(this@MainActivity) }
        btnBatteryOptimization.setOnClickListener { PermissionManager.requestIgnoreBatteryOptimizations(this@MainActivity) }
        btnSelectApps.setOnClickListener { startActivity(Intent(this@MainActivity, AppSelectionActivity::class.java)) }
        btnSendTest.setOnClickListener { sendTest() }
        btnForgetDevice.setOnClickListener { forget() }
        btnBeReceiver.setOnClickListener { startReceiver() }
        btnBeSender.setOnClickListener { startSender() }
    }

    private fun sendTest() {
        val s = serviceBinding.service ?: return toast(R.string.toast_please_connect_first)
        s.sendData(getString(R.string.test_notification_payload))
        toast(R.string.toast_test_data_sent)
    }

    private fun forget() {
        serviceBinding.service?.forgetDevice(); ui.updateStatus(BluetoothEvent.DISCONNECTED)
        toast(R.string.toast_device_forgotten)
    }

    private fun startReceiver() = withPermissions {
        BluetoothServiceStarter.start(this); ui.updateStatus(BluetoothEvent.LISTENING)
        serviceBinding.service?.startServer()
    }

    @SuppressLint("MissingPermission")
    private fun startSender() = withPermissions {
        BluetoothServiceStarter.start(this)
        BluetoothUIHelper.showDeviceSelectionDialog(this) { device ->
            if (!PermissionManager.hasBluetoothConnectPermission(this)) return@showDeviceSelectionDialog
            ui.updateStatus(BluetoothEvent.connecting(device.name ?: getString(R.string.label_unknown_device)))
            serviceBinding.service?.connectToDevice(device)
        }
    }

    private fun withPermissions(action: () -> Unit) {
        if (PermissionManager.checkPermissionsGranted(this)) action() else toast(R.string.toast_grant_permissions_first)
    }

    private fun handleEvent(event: String) {
        Log.d("NotiflyMA", "handleEvent: $event")
        when {
            event == BluetoothEvent.CONNECTED -> ui.updateStatus(BluetoothEvent.CONNECTED)
            event == BluetoothEvent.DISCONNECTED -> ui.updateStatus(BluetoothEvent.DISCONNECTED)
            event.startsWith(BluetoothEvent.CONNECTED_PREFIX) ->
                ui.updateStatus(BluetoothEvent.CONNECTED, event.removePrefix(BluetoothEvent.CONNECTED_PREFIX))
            event.startsWith(BluetoothEvent.CONNECTING_PREFIX) ->
                ui.updateStatus(BluetoothEvent.CONNECTING, event.removePrefix(BluetoothEvent.CONNECTING_PREFIX))
            event.startsWith(BluetoothEvent.DATA_PREFIX) -> showRemote(event)
            else -> Unit
        }
    }

    private fun showRemote(event: String) {
        val rawData = event.removePrefix(BluetoothEvent.DATA_PREFIX)
        Log.d("NotiflyMA", "showRemote rawData: $rawData")
        // Split by newlines if multiple messages were bundled, or just handle the single message
        rawData.split("\n")
            .map { it.trim() }
            .filter { it.isNotBlank() }
            .forEach { line ->
                Log.d("NotiflyMA", "Parsing line: $line")
                NotificationPayload.parse(line)?.let { payload ->
                    Log.d("NotiflyMA", "Showing notification for: ${payload.appName}")
                    NotificationHelper.showLocalNotification(this, payload)
                }
            }
    }

    private fun toast(res: Int) = Toast.makeText(this, res, Toast.LENGTH_SHORT).show()
}
