package com.khaizro.notifly.utils

import android.annotation.SuppressLint
import android.app.AlertDialog
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothManager
import android.content.Context
import android.util.Log
import android.widget.Toast
import com.khaizro.notifly.R

object BluetoothUIHelper {

    @SuppressLint("MissingPermission")
    fun showDeviceSelectionDialog(
        context: Context,
        onDeviceSelected: (BluetoothDevice) -> Unit
    ) {
        try {
            val bluetoothManager = context.getSystemService(Context.BLUETOOTH_SERVICE) as? BluetoothManager
            val bluetoothAdapter = bluetoothManager?.adapter
            
            if (bluetoothAdapter == null || !bluetoothAdapter.isEnabled) {
                Toast.makeText(context, context.getString(R.string.toast_enable_bluetooth), Toast.LENGTH_SHORT).show()
                return
            }

            val pairedDevices = mutableListOf<BluetoothDevice>()
            val paired = bluetoothAdapter.bondedDevices
            val names = mutableListOf<String>()
            
            paired?.forEach {
                pairedDevices.add(it)
                names.add(it.name ?: context.getString(R.string.label_unknown_device))
            }

            if (names.isEmpty()) {
                Toast.makeText(context, context.getString(R.string.toast_no_paired_devices_found), Toast.LENGTH_SHORT).show()
                return
            }

            val builder = AlertDialog.Builder(context, android.R.style.Theme_DeviceDefault_Dialog_Alert)
            builder.setTitle(context.getString(R.string.status_select_device))
            builder.setItems(names.toTypedArray()) { _, which ->
                val device = pairedDevices[which]
                onDeviceSelected(device)
            }
            builder.setNegativeButton(android.R.string.cancel, null)
            builder.show()

        } catch (e: Exception) {
            Log.e("BluetoothUIHelper", "Sender mode error", e)
            Toast.makeText(context, "Could not load devices", Toast.LENGTH_SHORT).show()
        }
    }
}