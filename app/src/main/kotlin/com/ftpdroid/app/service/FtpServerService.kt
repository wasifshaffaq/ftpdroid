package com.ftpdroid.app.service

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.content.pm.ServiceInfo
import android.os.Build
import android.os.IBinder
import androidx.core.app.NotificationCompat
import com.ftpdroid.app.MainActivity
import com.ftpdroid.app.R
import com.ftpdroid.app.data.local.prefs.ServerPreferences
import com.ftpdroid.app.data.network.ftp.FtpServerManager
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.*
import javax.inject.Inject

@AndroidEntryPoint
class FtpServerService : Service() {

    @Inject
    lateinit var ftpServerManager: FtpServerManager

    @Inject
    lateinit var serverPreferences: ServerPreferences

    private val serviceScope = CoroutineScope(Dispatchers.IO + SupervisorJob())
    private var stateJob: Job? = null

    override fun onCreate() {
        super.onCreate()
        createNotificationChannel()
        observeServerState()
    }

    private fun observeServerState() {
        stateJob?.cancel()
        stateJob = serviceScope.launch {
            var isFirstEmission = true
            ftpServerManager.state.collect { state ->
                val initialStopped = isFirstEmission && state is FtpServerManager.ServerState.Stopped
                isFirstEmission = false
                
                if (initialStopped) {
                    android.util.Log.d("FtpServerService", "Ignoring initial Stopped state")
                    return@collect
                }

                android.util.Log.d("FtpServerService", "Observed state: $state")
                when (state) {
                    is FtpServerManager.ServerState.Running -> {
                        updateNotification("FTP Server running on port ${state.port}")
                    }
                    is FtpServerManager.ServerState.Stopped -> {
                        android.util.Log.d("FtpServerService", "Server stopped, stopping service")
                        stopForeground(STOP_FOREGROUND_REMOVE)
                        stopSelf()
                    }
                    is FtpServerManager.ServerState.Error -> {
                        updateNotification("Error: ${state.message}")
                        // Optionally stop after a delay or keep showing error
                        delay(3000)
                        stopForeground(STOP_FOREGROUND_REMOVE)
                        stopSelf()
                    }
                    is FtpServerManager.ServerState.Starting -> {
                        updateNotification("Starting FTP Server on port ${state.port}...")
                    }
                }
            }
        }
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        val action = intent?.action
        android.util.Log.d("FtpServerService", "onStartCommand received action: $action")
        when (action) {
            ACTION_START -> {
                // For Android 12+, we MUST call startForeground quickly
                val notification = createNotification("Starting FTP Server...")
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                    startForeground(NOTIFICATION_ID, notification, ServiceInfo.FOREGROUND_SERVICE_TYPE_DATA_SYNC)
                } else {
                    startForeground(NOTIFICATION_ID, notification)
                }
                startServer()
            }
            ACTION_STOP -> stopServer()
            else -> android.util.Log.w("FtpServerService", "Unknown action received: $action")
        }
        return START_STICKY
    }

    private fun startServer() {
        val currentState = ftpServerManager.state.value
        android.util.Log.d("FtpServerService", "startServer() called, current state: $currentState")
        if (currentState is FtpServerManager.ServerState.Running || 
            currentState is FtpServerManager.ServerState.Starting) return
        
        val config = serverPreferences.getServerConfig()
        serviceScope.launch {
            try {
                ftpServerManager.start(config)
            } catch (e: Exception) {
                android.util.Log.e("FtpServerService", "Error in startServer coroutine", e)
            }
        }
    }

    private fun stopServer() {
        android.util.Log.d("FtpServerService", "stopServer() called")
        ftpServerManager.stop()
    }

    private fun updateNotification(text: String) {
        val notification = createNotification(text)
        val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.notify(NOTIFICATION_ID, notification)
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                "FTP Server",
                NotificationManager.IMPORTANCE_LOW
            ).apply {
                description = "FTP Server running notification"
            }
            val manager = getSystemService(NotificationManager::class.java)
            manager.createNotificationChannel(channel)
        }
    }

    private fun createNotification(text: String): android.app.Notification {
        val stopIntent = Intent(this, FtpServerService::class.java).apply {
            action = ACTION_STOP
        }
        val stopPending = PendingIntent.getService(
            this, 0, stopIntent, PendingIntent.FLAG_IMMUTABLE
        )

        val mainIntent = Intent(this, MainActivity::class.java)
        val mainPending = PendingIntent.getActivity(
            this, 0, mainIntent, PendingIntent.FLAG_IMMUTABLE
        )

        return NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle("FTP Server")
            .setContentText(text)
            .setSmallIcon(R.drawable.ic_server)
            .setContentIntent(mainPending)
            .addAction(R.drawable.ic_stop, "Stop", stopPending)
            .setOngoing(true)
            .build()
    }

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onDestroy() {
        android.util.Log.d("FtpServerService", "onDestroy() called")
        ftpServerManager.stop()
        serviceScope.cancel()
        super.onDestroy()
    }

    companion object {
        const val ACTION_START = "com.ftpdroid.app.ACTION_START_SERVER"
        const val ACTION_STOP = "com.ftpdroid.app.ACTION_STOP_SERVER"
        const val NOTIFICATION_ID = 1
        const val CHANNEL_ID = "ftp_server_channel"
    }
}
