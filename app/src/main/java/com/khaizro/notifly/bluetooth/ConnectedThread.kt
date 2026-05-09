package com.khaizro.notifly.bluetooth

import android.bluetooth.BluetoothSocket
import android.util.Log
import java.io.IOException

class ConnectedThread(
    private val socket: BluetoothSocket,
    private val onEvent: (String) -> Unit,
) : Thread("NotiflyConnectedThread") {
    private val input = socket.inputStream
    private val output = socket.outputStream
    private val buffer = ByteArray(1024)
    private val incoming = StringBuilder()

    @Volatile
    private var isClosed = false

    override fun run() {
        Log.d("ConnThread", "ConnectedThread started")
        while (!isInterrupted && !isClosed) {
            val count = try {
                input.read(buffer)
            } catch (e: IOException) {
                if (!isClosed) {
                    isClosed = true
                    Log.d("ConnThread", "Input stream read failed: ${e.message}")
                    onEvent(BluetoothEvent.DISCONNECTED)
                    runCatching { socket.close() }
                }
                return
            }
            if (count > 0) {
                val chunk = String(buffer, 0, count)
                Log.d("ConnThread", "Read $count bytes: ${chunk.replace("\n", "\\n")}")
                publishLines(chunk)
            }
        }
    }

    private fun publishLines(chunk: String) {
        incoming.append(chunk)
        while (true) {
            val idx = incoming.indexOf("\n")
            if (idx < 0) return
            val line = incoming.substring(0, idx).trimEnd('\r')
            incoming.delete(0, idx + 1)
            Log.d("ConnThread", "Extracted line: $line")
            if (line.isNotBlank()) onEvent(BluetoothEvent.dataMessage(line))
        }
    }

    fun write(bytes: ByteArray) {
        if (isClosed) {
            Log.w("ConnThread", "Attempted to write to a closed thread")
            return
        }
        try {
            Log.d("ConnThread", "Writing ${bytes.size} bytes...")
            output.write(bytes)
            output.write('\n'.code)
            output.flush()
            Log.d("ConnThread", "Write and flush successful")
        } catch (e: IOException) {
            if (!isClosed) {
                isClosed = true
                Log.e("ConnThread", "Write failed", e)
                onEvent(BluetoothEvent.DISCONNECTED)
                runCatching { socket.close() }
            }
        }
    }

    fun cancel() {
        Log.d("ConnThread", "Cancelling ConnectedThread")
        isClosed = true
        interrupt()
        runCatching { socket.close() }
    }
}
