package com.ftpdroid.app.service

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.IBinder
import androidx.core.app.NotificationCompat
import com.ftpdroid.app.MainActivity
import com.ftpdroid.app.R
import com.ftpdroid.app.data.local.prefs.ServerPreferences
import com.ftpdroid.app.data.network.ftp.FtpServerManager
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class FtpServerService : Service() {

    @Inject
    lateinit var ftpServerManager: FtpServerManager

    @Inject
    lateinit var serverPreferences: ServerPreferences

    private val serviceScope = CoroutineScope(Dispatchers.IO + Job())
    private var isRunning = false

    override fun onCreate() {
        super.onCreate()
        createNotificationChannel()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        val action = intent?.action
        android.util.Log.d("FtpServerService", "onStartCommand received action: $action")
        when (action) {
            ACTION_START -> startServer()
            ACTION_STOP -> stopServer()
            else -> android.util.Log.w("FtpServerService", "Unknown action received: $action")
        }
        return START_STICKY
    }

    private fun startServer() {
        android.util.Log.d("FtpServerService", "startServer() called, isRunning: $isRunning")
        if (isRunning) return
        
        val config = serverPreferences.getServerConfig()
        android.util.Log.d("FtpServerService", "Starting server with config: $config")
        serviceScope.launch {
            try {
                ftpServerManager.start(config)
                isRunning = true
                android.util.Log.d("FtpServerService", "Server manager start() completed successfully")
                val notification = createNotification("FTP Server running on port ${config.port}")
                startForeground(NOTIFICATION_ID, notification)
            } catch (e: Exception) {
                android.util.Log.e("FtpServerService", "Error in startServer coroutine", e)
                stopSelf()
            }
        }
    }

    private fun stopServer() {
        ftpServerManager.stop()
        isRunning = false
        stopForeground(STOP_FOREGROUND_REMOVE)
        stopSelf()
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
        ftpServerManager.stop()
        super.onDestroy()
    }

    companion object {
        const val ACTION_START = "com.ftpdroid.app.ACTION_START_SERVER"
        const val ACTION_STOP = "com.ftpdroid.app.ACTION_STOP_SERVER"
        const val NOTIFICATION_ID = 1
        const val CHANNEL_ID = "ftp_server_channel"
    }
}
