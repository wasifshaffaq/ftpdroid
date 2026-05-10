package com.ftpdroid.app.data.network.ftp

import android.content.Context
import android.os.Environment
import android.util.Log
import com.ftpdroid.app.data.local.db.dao.ConnectionLogDao
import com.ftpdroid.app.data.local.db.entity.ConnectionLogEntity
import com.ftpdroid.app.domain.model.LogEventType
import com.ftpdroid.app.domain.model.ServerConfig
import com.ftpdroid.app.domain.model.Transfer
import com.ftpdroid.app.domain.model.TransferStatus
import com.ftpdroid.app.domain.model.TransferType
import com.ftpdroid.app.domain.repository.TransferRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import org.apache.ftpserver.ConnectionConfigFactory
import org.apache.ftpserver.FtpServer
import org.apache.ftpserver.FtpServerFactory
import org.apache.ftpserver.ftplet.DefaultFtplet
import org.apache.ftpserver.ftplet.FtpReply
import org.apache.ftpserver.ftplet.FtpRequest
import org.apache.ftpserver.ftplet.FtpSession
import org.apache.ftpserver.ftplet.FtpletContext
import org.apache.ftpserver.ftplet.FtpletResult
import org.apache.ftpserver.listener.ListenerFactory
import org.apache.ftpserver.usermanager.ClearTextPasswordEncryptor
import org.apache.ftpserver.usermanager.PropertiesUserManagerFactory
import org.apache.ftpserver.usermanager.impl.BaseUser
import org.apache.ftpserver.usermanager.impl.ConcurrentLoginPermission
import org.apache.ftpserver.usermanager.impl.WritePermission
import java.io.File
import java.net.Inet4Address
import java.net.NetworkInterface
import java.util.concurrent.atomic.AtomicInteger
import javax.inject.Inject
import javax.inject.Singleton

import java.util.concurrent.ConcurrentHashMap

sealed class ConnectionEvent {
    data class Connected(val ip: String, val username: String) : ConnectionEvent()
    data class Disconnected(val ip: String, val username: String, val durationSec: Long) : ConnectionEvent()
    data class FileDelete(val path: String) : ConnectionEvent()
    data class TransferStarted(val sessionId: String, val path: String, val type: TransferType, val totalSize: Long) : ConnectionEvent()
    data class TransferFinished(val sessionId: String, val path: String, val size: Long, val success: Boolean, val type: TransferType) : ConnectionEvent()
}

class LoggingFtplet(
    private val scope: CoroutineScope,
    private val events: MutableSharedFlow<ConnectionEvent>,
    private val clientCount: AtomicInteger,
    private val onClientCountChanged: (Int) -> Unit,
    private val startTimes: MutableMap<String, Long> = ConcurrentHashMap()
) : DefaultFtplet() {

    override fun init(ftpletContext: FtpletContext?) {}

    private fun emitEvent(event: ConnectionEvent) {
        scope.launch {
            events.emit(event)
        }
    }

    override fun beforeCommand(session: FtpSession?, request: FtpRequest?): FtpletResult {
        val clientIp = session?.clientAddress?.address?.hostAddress ?: return FtpletResult.DEFAULT
        val command = request?.command?.uppercase() ?: return FtpletResult.DEFAULT
        val sessionId = session?.sessionId?.toString() ?: ""

        when (command) {
            "USER" -> {
                startTimes[clientIp] = System.currentTimeMillis()
            }
            "DELE", "RMD" -> {
                val path = request.argument ?: ""
                emitEvent(ConnectionEvent.FileDelete(path))
            }
            "RETR" -> {
                val path = request.argument ?: ""
                val ftpFile = try { session?.fileSystemView?.getFile(path) } catch (e: Exception) { null }
                val size = ftpFile?.size ?: 0L
                val absolutePath = ftpFile?.absolutePath ?: path
                val homeDir = session?.user?.homeDirectory ?: ""
                val physicalPath = if (homeDir.isNotEmpty() && absolutePath.isNotEmpty()) {
                    val relativePath = if (absolutePath.startsWith("/")) absolutePath.substring(1) else absolutePath
                    File(homeDir, relativePath).absolutePath
                } else {
                    path
                }
                emitEvent(ConnectionEvent.TransferStarted(sessionId, physicalPath, TransferType.DOWNLOAD, size))
            }
            "STOR", "APPE" -> {
                val path = request.argument ?: ""
                val ftpFile = try { session?.fileSystemView?.getFile(path) } catch (e: Exception) { null }
                val absolutePath = ftpFile?.absolutePath ?: path
                val homeDir = session?.user?.homeDirectory ?: ""
                val physicalPath = if (homeDir.isNotEmpty() && absolutePath.isNotEmpty()) {
                    val relativePath = if (absolutePath.startsWith("/")) absolutePath.substring(1) else absolutePath
                    File(homeDir, relativePath).absolutePath
                } else {
                    path
                }
                Log.d("FTPD_POLL", "Physical path resolved: $physicalPath | File exists: ${File(physicalPath).exists()}")
                emitEvent(ConnectionEvent.TransferStarted(sessionId, physicalPath, TransferType.UPLOAD, 0L))
            }
        }
        return FtpletResult.DEFAULT
    }

    override fun afterCommand(session: FtpSession?, request: FtpRequest?, reply: FtpReply?): FtpletResult {
        val clientIp = session?.clientAddress?.address?.hostAddress ?: return FtpletResult.DEFAULT
        val command = request?.command?.uppercase() ?: return FtpletResult.DEFAULT
        val sessionId = session?.sessionId?.toString() ?: ""
        
        val isSuccess = reply?.code == 226 || reply?.code == 250

        when (command) {
            "RETR" -> {
                val path = request.argument ?: ""
                val ftpFile = try { session?.fileSystemView?.getFile(path) } catch (e: Exception) { null }
                val size = ftpFile?.size ?: 0L
                emitEvent(ConnectionEvent.TransferFinished(sessionId, path, size, isSuccess, TransferType.DOWNLOAD))
            }
            "STOR", "APPE" -> {
                val path = request.argument ?: ""
                val ftpFile = try { session?.fileSystemView?.getFile(path) } catch (e: Exception) { null }
                // For uploads, the size in ftpFile might not be updated yet, so we'll check the physical file later in the collector if needed,
                // but let's try to get it here first.
                val size = ftpFile?.size ?: 0L
                emitEvent(ConnectionEvent.TransferFinished(sessionId, path, size, isSuccess, TransferType.UPLOAD))
            }
        }
        return FtpletResult.DEFAULT
    }

    override fun onConnect(session: FtpSession?): FtpletResult {
        val clientIp = session?.clientAddress?.address?.hostAddress ?: return FtpletResult.DEFAULT
        val user = session?.user?.name ?: "anonymous"
        val count = clientCount.incrementAndGet()
        onClientCountChanged(count)
        emitEvent(ConnectionEvent.Connected(clientIp, user))
        return FtpletResult.DEFAULT
    }

    override fun onDisconnect(session: FtpSession?): FtpletResult {
        val clientIp = session?.clientAddress?.address?.hostAddress ?: return FtpletResult.DEFAULT
        val user = session?.user?.name ?: "anonymous"
        val count = clientCount.decrementAndGet()
        onClientCountChanged(count)
        val durationSec = startTimes[clientIp]?.let { (System.currentTimeMillis() - it) / 1000 } ?: 0L
        emitEvent(ConnectionEvent.Disconnected(clientIp, user, durationSec))
        startTimes.remove(clientIp)
        return FtpletResult.DEFAULT
    }

    override fun destroy() {}
}

@Singleton
class FtpServerManager @Inject constructor(
    @dagger.hilt.android.qualifiers.ApplicationContext private val context: Context,
    private val connectionLogDao: ConnectionLogDao,
    private val transferRepository: TransferRepository
) {
    private val TAG = "FtpServerManager"
    private var ftpServer: FtpServer? = null
    private val managerScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
    private val clientCount = AtomicInteger(0)

    sealed class ServerState {
        object Stopped : ServerState()
        data class Starting(val port: Int) : ServerState()
        data class Running(val port: Int, val ipAddress: String, val connectedClients: Int) : ServerState()
        data class Error(val message: String) : ServerState()
    }

    private val _state = MutableStateFlow<ServerState>(ServerState.Stopped)
    val state: StateFlow<ServerState> = _state.asStateFlow()

    private val _connectionEvents = MutableSharedFlow<ConnectionEvent>(extraBufferCapacity = 128)
    val connectionEvents: SharedFlow<ConnectionEvent> = _connectionEvents.asSharedFlow()

    private val activeTransfers = ConcurrentHashMap<String, Long>()
    private var progressJob: Job? = null

    var startTime = 0L
        private set

    init {
        // Start collecting events for database persistence permanently
        managerScope.launch {
            _connectionEvents.collect { event ->
                try {
                    val logEntry = when (event) {
                        is ConnectionEvent.Connected -> ConnectionLogEntity(
                            ipAddress = event.ip,
                            username = event.username,
                            eventType = LogEventType.CONNECT.name,
                            path = "",
                            message = "Connected"
                        )
                        is ConnectionEvent.Disconnected -> ConnectionLogEntity(
                            ipAddress = event.ip,
                            username = event.username,
                            eventType = LogEventType.DISCONNECT.name,
                            path = "",
                            message = "Disconnected (Duration: ${event.durationSec}s)"
                        )
                        is ConnectionEvent.TransferStarted -> {
                            val transfer = Transfer(
                                profileId = -1,
                                type = event.type,
                                localPath = "remote",
                                remotePath = event.path,
                                totalBytes = event.totalSize,
                                transferredBytes = 0,
                                status = TransferStatus.IN_PROGRESS
                            )
                            val id = transferRepository.insertTransfer(transfer)
                            activeTransfers[event.sessionId] = id
                            startProgressPolling()
                            null
                        }
                        is ConnectionEvent.TransferFinished -> {
                            val transferId = activeTransfers.remove(event.sessionId)
                            if (activeTransfers.isEmpty()) stopProgressPolling()
                            
                            if (transferId != null) {
                                val transfer = transferRepository.getTransferById(transferId)
                                if (transfer != null) {
                                    val finalSize = if (event.size <= 0 && transfer.type == TransferType.UPLOAD) {
                                        File(transfer.remotePath).length()
                                    } else {
                                        event.size
                                    }
                                    
                                    transferRepository.updateTransfer(transfer.copy(
                                        transferredBytes = finalSize,
                                        totalBytes = if (transfer.totalBytes <= 0) finalSize else transfer.totalBytes,
                                        status = if (event.success) TransferStatus.COMPLETED else TransferStatus.FAILED,
                                        completedAt = if (event.success) System.currentTimeMillis() else null,
                                        speedBytesPerSec = 0
                                    ))
                                }
                            }
                            ConnectionLogEntity(
                                ipAddress = "",
                                username = "anonymous",
                                eventType = if (event.type == TransferType.DOWNLOAD) LogEventType.DOWNLOAD.name else LogEventType.UPLOAD.name,
                                path = event.path,
                                message = if (event.success) "Completed" else "Failed"
                            )
                        }
                        is ConnectionEvent.FileDelete -> ConnectionLogEntity(
                            ipAddress = "",
                            username = "anonymous",
                            eventType = LogEventType.DELETE.name,
                            path = event.path,
                            message = "Deleted"
                        )
                    }
                    logEntry?.let { connectionLogDao.insertLog(it) }
                } catch (e: Exception) {
                    Log.e(TAG, "Error persisting connection event", e)
                }
            }
        }
    }

    private fun startProgressPolling() {
        if (progressJob?.isActive == true) return
        progressJob = managerScope.launch {
            delay(500) // Small delay to let data connection establish
            while (activeTransfers.isNotEmpty()) {
                val server = ftpServer ?: break
                
                activeTransfers.forEach { (sessionId, transferId) ->
                    val transfer = transferRepository.getTransferById(transferId) ?: return@forEach
                    
                    // Re-checking the file size on disk for uploads (STOR) is the most reliable high-level way.
                    if (transfer.type == TransferType.UPLOAD) {
                        val file = File(transfer.remotePath) // In server mode, remotePath is the local path on device
                        val exists = file.exists()
                        val currentSize = if (exists) file.length() else 0L
                        Log.d("FTPD_POLL", "Poll tick | id=$transferId | exists=$exists | size=$currentSize | path=${transfer.remotePath}")
                        if (exists) {
                            val previousSize = transfer.transferredBytes
                            val speed = if (currentSize > previousSize) currentSize - previousSize else 0L
                            transferRepository.updateTransferProgress(transferId, currentSize, speed)
                        }
                    }
                }
                delay(1000)
            }
        }
    }

    private fun stopProgressPolling() {
        progressJob?.cancel()
        progressJob = null
    }

    suspend fun start(config: ServerConfig) {
        Log.d(TAG, "start() called with config: $config")
        if (ftpServer != null && !ftpServer!!.isStopped) {
            Log.d(TAG, "Server already running, ignoring start request")
            return
        }
        _state.value = ServerState.Starting(config.port)

        startTime = System.currentTimeMillis()
        try {
            Log.d(TAG, "Starting FTP server setup on port ${config.port}")
            val serverFactory = FtpServerFactory()

            // Step 1: Enable anonymous login at the connection level
            val connectionConfig = ConnectionConfigFactory().apply {
                isAnonymousLoginEnabled = true
                maxLogins = config.maxConnections
                maxAnonymousLogins = config.maxConnections
            }.createConnectionConfig()
            serverFactory.connectionConfig = connectionConfig
            Log.d(TAG, "Connection config created")

            val ftplet = LoggingFtplet(
                managerScope,
                _connectionEvents,
                clientCount,
                onClientCountChanged = { count ->
                    val currentState = _state.value
                    if (currentState is ServerState.Running) {
                        _state.value = currentState.copy(connectedClients = count)
                    }
                }
            )
            serverFactory.ftplets = mapOf("logging" to ftplet)
            Log.d(TAG, "Ftplet configured")

            val listenerFactory = ListenerFactory().apply {
                port = config.port
                serverAddress = "0.0.0.0"
            }
            Log.d(TAG, "Listener factory created for port ${config.port}")


            // Step 2: Create a real .properties file with explicit password encryptor
            val propsFile = File(context.cacheDir, "ftp_users.properties").also {
                if (!it.exists()) {
                    val created = it.createNewFile()
                    Log.d(TAG, "Users properties file created: $created at ${it.absolutePath}")
                }
            }
            val userManagerFactory = PropertiesUserManagerFactory().apply {
                file = propsFile
                passwordEncryptor = ClearTextPasswordEncryptor()
            }
            val manager = userManagerFactory.createUserManager()
            Log.d(TAG, "User manager created")

            // Step 3: Save the anonymous user explicitly with empty string password
            if (true) {
                val rootPath = if (config.anonymousRootPath.isNotEmpty()) {
                    config.anonymousRootPath
                } else {
                    Environment.getExternalStorageDirectory().absolutePath
                }
                
                val anonymousUser = BaseUser().apply {
                    name = "anonymous"
                    password = ""                        // empty string, NOT null
                    homeDirectory = rootPath
                    setEnabled(true)
                    authorities = listOf(WritePermission(), ConcurrentLoginPermission(5, 5))
                }
                manager.save(anonymousUser)
                Log.d(TAG, "Anonymous user configured with home: ${anonymousUser.homeDirectory}")
            }
            
            serverFactory.userManager = manager
            serverFactory.addListener("default", listenerFactory.createListener())
            
            Log.d(TAG, "Creating server instance...")
            ftpServer = serverFactory.createServer()
            Log.d(TAG, "Starting server instance...")
            ftpServer!!.start()
            
            val ip = getLocalIpAddress()
            Log.d(TAG, "FTP server successfully started at $ip:${config.port}")
            _state.value = ServerState.Running(config.port, ip, clientCount.get())
        } catch (e: Exception) {
            Log.e(TAG, "Critical: FTP Server failed to start", e)
            _state.value = ServerState.Error(e.message ?: "Unknown error")
        }
    }

    fun stop() {
        Log.d(TAG, "stop() called")
        val server = ftpServer
        ftpServer = null // Set to null immediately to allow restart attempts
        
        try {
            server?.stop()
        } catch (e: Exception) {
            Log.e(TAG, "Error stopping FTP server", e)
        }
        
        // Reset state
        clientCount.set(0)
        activeTransfers.clear()
        stopProgressPolling()
        
        _state.value = ServerState.Stopped
        Log.d(TAG, "Server stopped and resources cleared")
    }

    fun getLocalIpAddress(): String {
        return try {
            NetworkInterface.getNetworkInterfaces().toList()
                .flatMap { it.inetAddresses.toList() }
                .first { !it.isLoopbackAddress && it is Inet4Address }
                .hostAddress ?: "0.0.0.0"
        } catch (_: Exception) { "0.0.0.0" }
    }
}
