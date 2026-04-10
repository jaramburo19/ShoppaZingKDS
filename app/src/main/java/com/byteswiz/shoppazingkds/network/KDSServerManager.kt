package com.byteswiz.shoppazingkds.network

import android.util.Log
import com.byteswiz.parentmodel.CompleteOrderRequest
import com.byteswiz.parentmodel.ParentModel
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import java.io.EOFException
import java.io.ObjectInputStream
import java.io.ObjectOutputStream
import java.net.InetSocketAddress
import java.net.ServerSocket
import java.net.Socket
import java.net.SocketException
import java.net.SocketTimeoutException
import java.util.concurrent.CopyOnWriteArrayList
import java.util.concurrent.atomic.AtomicInteger

class KDSServerManager {
    private val TAG = "KDSServer"
    private val PORT = 8081

    private var serverSocket: ServerSocket? = null
    private var isRunning = false
    private val serverScope = CoroutineScope(Dispatchers.IO + SupervisorJob())

    class ClientConnection(val socket: Socket, val oos: ObjectOutputStream) {
        val writeMutex = Mutex()
    }
    private val activeClients = CopyOnWriteArrayList<ClientConnection>()
    private val clientCount = AtomicInteger(0)

    private val _serverState = MutableStateFlow("Disconnected")
    val serverState = _serverState.asStateFlow()

    private val _incomingOrders = MutableSharedFlow<ParentModel>(extraBufferCapacity = 10)
    val incomingOrders = _incomingOrders.asSharedFlow()

    // FIX: Removed the '=' and used standard curly braces to prevent the recursive compiler bug
    suspend fun startServer() {
        withContext(Dispatchers.IO) {
            if (isRunning) return@withContext
            try {
                serverSocket = ServerSocket()
                serverSocket?.reuseAddress = true
                serverSocket?.bind(InetSocketAddress(PORT))

                isRunning = true
                _serverState.value = "Listening on Port $PORT..."

                while (isRunning && isActive) {
                    val clientSocket = serverSocket?.accept() ?: continue
                    val currentCount = clientCount.incrementAndGet()
                    _serverState.value = "Connected ($currentCount devices)"
                    Log.d(TAG, "New POS connected. Total: $currentCount")

                    serverScope.launch { handleClient(clientSocket) }
                }
            } catch (e: Exception) {
                Log.e(TAG, "Server stopped", e)
                isRunning = false
                _serverState.value = "Disconnected"

                // Auto-reconnect if it crashes
                delay(5000)
                startServer() // <-- This recursive call is perfectly safe now!
            }
        }
    }

    // FIX: Removed the '=' here as well just to be perfectly safe
    private suspend fun handleClient(socket: Socket) {
        withContext(Dispatchers.IO) {
            var oos: ObjectOutputStream? = null
            var ois: ObjectInputStream? = null
            var clientConn: ClientConnection? = null

            val pingJob = launch {
                while (isActive && isRunning) {
                    delay(3000)
                    try {
                        clientConn?.writeMutex?.withLock {
                            oos?.writeObject("PING")
                            oos?.reset()
                            oos?.flush()
                        }
                    } catch (e: Exception) {
                        break
                    }
                }
            }

            try {
                socket.soTimeout = 12000

                oos = ObjectOutputStream(socket.getOutputStream())
                oos.flush()
                ois = ObjectInputStream(socket.getInputStream())

                clientConn = ClientConnection(socket, oos)
                activeClients.add(clientConn)

                while (isActive && isRunning) {
                    try {
                        val incomingObj = ois.readObject() ?: break

                        if (incomingObj is String && incomingObj == "PING") {
                            continue
                        }

                        val newOrder = incomingObj as? ParentModel
                        if (newOrder != null) {
                            _incomingOrders.emit(newOrder)
                        }
                    } catch (e: SocketTimeoutException) {
                        Log.e(TAG, "Socket Timeout! POS died silently. Dropping connection.")
                        break
                    }
                }
            } catch (e: EOFException) {
                Log.d(TAG, "POS disconnected gracefully")
            } catch (e: SocketException) {
                Log.d(TAG, "POS connection lost")
            } catch (e: Exception) {
                Log.e(TAG, "Error handling POS client", e)
            } finally {
                pingJob.cancel()

                if (clientConn != null) {
                    activeClients.remove(clientConn)
                }

                val remaining = clientCount.decrementAndGet()
                _serverState.value = if (remaining > 0) "Connected ($remaining devices)" else "Listening on Port $PORT..."

                try { ois?.close() } catch (e: Exception) {}
                try { oos?.close() } catch (e: Exception) {}
                try { socket.close() } catch (e: Exception) {}
            }
        }
    }

    // FIX: Explicitly declared it returns a Boolean, and used 'return withContext'
    suspend fun sendOrderComplete(request: CompleteOrderRequest): Boolean {
        return withContext(Dispatchers.IO) {
            var overallSuccess = true

            for (client in activeClients) {
                try {
                    client.writeMutex.withLock {
                        client.oos.writeObject(request)
                        client.oos.reset()
                        client.oos.flush()
                    }
                } catch (e: Exception) {
                    Log.e(TAG, "Failed to send completion status to a POS client", e)
                    overallSuccess = false
                }
            }

            overallSuccess && activeClients.isNotEmpty()
        }
    }

    fun stopServer() {
        isRunning = false
        serverSocket?.close()
        activeClients.clear()
        clientCount.set(0)
    }
}