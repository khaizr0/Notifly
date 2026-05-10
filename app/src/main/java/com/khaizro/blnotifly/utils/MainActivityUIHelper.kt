package com.khaizro.blnotifly.utils

import android.app.Activity
import android.content.res.ColorStateList
import android.view.View
import android.widget.*
import androidx.core.content.ContextCompat
import com.google.android.material.button.MaterialButton
import com.khaizro.blnotifly.R
import com.khaizro.blnotifly.bluetooth.BluetoothEvent

class MainActivityUIHelper(private val activity: Activity) {
    lateinit var btnPermission: Button
    lateinit var btnNotificationAccess: Button
    lateinit var btnBatteryOptimization: Button
    lateinit var btnSendTest: Button
    lateinit var btnForgetDevice: Button
    lateinit var btnSelectApps: Button
    lateinit var btnBeReceiver: Button
    lateinit var btnBeSender: Button
    private lateinit var statusText: TextView
    private lateinit var statusDeviceName: TextView
    private lateinit var statusIcon: ImageView

    fun setupViews() {
        statusText = activity.findViewById(R.id.statusText)
        statusDeviceName = activity.findViewById(R.id.statusDeviceName)
        statusIcon = activity.findViewById(R.id.statusIcon)
        btnPermission = activity.findViewById(R.id.btnPermission)
        btnNotificationAccess = activity.findViewById(R.id.btnNotificationAccess)
        btnBatteryOptimization = activity.findViewById(R.id.btnBatteryOptimization)
        btnSendTest = activity.findViewById(R.id.btnSendTest)
        btnForgetDevice = activity.findViewById(R.id.btnForgetDevice)
        btnSelectApps = activity.findViewById(R.id.btnSelectApps)
        btnBeReceiver = activity.findViewById(R.id.btnBeReceiver)
        btnBeSender = activity.findViewById(R.id.btnBeSender)
    }

    fun updatePermissionState() {
        val ready = PermissionManager.isNotificationAccessGranted(activity)
        updateBtn(btnPermission, PermissionManager.checkPermissionsGranted(activity))
        updateBtn(btnNotificationAccess, ready)
        updateBtn(btnBatteryOptimization, PermissionManager.isBatteryOptimizationIgnored(activity))
        statusText.setTextColor(ContextCompat.getColor(activity, if (ready) R.color.white else R.color.status_disconnected))
        if (!ready) statusText.text = activity.getString(R.string.error_re_enable_notification_access)
    }

    fun updateStatus(state: String, device: String = "") {
        val viewState = when (state) {
            BluetoothEvent.LISTENING -> Pair(R.string.status_listening, R.color.status_listening)
            BluetoothEvent.CONNECTING -> Pair(activity.getString(R.string.status_connecting, device), R.color.status_connecting)
            BluetoothEvent.CONNECTED -> Pair(R.string.status_connected, R.color.status_connected)
            else -> Pair(R.string.status_disconnected, R.color.status_disconnected)
        }
        val text = viewState.first
        statusText.text = if (text is Int) activity.getString(text) else text as String
        statusIcon.imageTintList = ColorStateList.valueOf(ContextCompat.getColor(activity, viewState.second))
        val visible = device.isNotBlank() && state in setOf(BluetoothEvent.CONNECTED, BluetoothEvent.CONNECTING)
        statusDeviceName.visibility = if (visible) View.VISIBLE else View.GONE
        if (visible) statusDeviceName.text = activity.getString(R.string.status_connected_device, device)
    }

    private fun updateBtn(b: Button, g: Boolean) {
        if (b !is MaterialButton) return
        b.setTextColor(ContextCompat.getColor(activity, if (g) R.color.status_connected else R.color.white))
        b.icon = if (g) ContextCompat.getDrawable(activity, android.R.drawable.checkbox_on_background) else null
        b.iconTint = if (g) ColorStateList.valueOf(ContextCompat.getColor(activity, R.color.status_connected)) else null
    }
}
