package com.khaizro.notifly.bluetooth

object BluetoothEvent {
    const val CONNECTED = "CONNECTED"
    const val DISCONNECTED = "DISCONNECTED"
    const val LISTENING = "LISTENING"
    const val CONNECTING = "CONNECTING"
    
    const val CONNECTING_PREFIX = "CONNECTING:"
    const val CONNECTED_PREFIX = "CONNECTED_TO:"
    const val DATA_PREFIX = "DATA:"
    const val ERROR_PREFIX = "ERROR:"

    fun errorEvent(msg: String?) = "$ERROR_PREFIX $msg"
    fun connectedEvent(name: String) = "$CONNECTED_PREFIX$name"
    fun dataMessage(content: String) = "$DATA_PREFIX$content"
    fun connecting(name: String) = "$CONNECTING_PREFIX$name"
}
