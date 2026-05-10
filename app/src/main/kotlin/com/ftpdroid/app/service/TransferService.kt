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
import com.ftpdroid.app.data.local.db.dao.TransferDao
import com.ftpdroid.app.data.network.ftp.FtpClientManager
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class TransferService : Service() {

    @Inject
    lateinit var transferDao: TransferDao

    @Inject
    lateinit var ftpClientManager: FtpClientManager

    private val serviceScope = CoroutineScope(Dispatchers.IO + Job())
    private var activeJobs = mutableMapOf<Long, Job>()

    override fun onCreate() {
        super.onCreate()
        createNotificationChannel()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        when (intent?.action) {
            ACTION_START_UPLOAD -> {
                val remotePath = intent.getStringExtra(EXTRA_REMOTE_PATH) ?: return START_NOT_STICKY
                val localPath = intent.getStringExtra(EXTRA_LOCAL_PATH) ?: return START_NOT_STICKY
                val transferId = intent.getLongExtra(EXTRA_TRANSFER_ID, -1L)
                if (transferId == -1L) return START_NOT_STICKY
                startUpload(transferId, localPath, remotePath)
            }
            ACTION_START_DOWNLOAD -> {
                val remotePath = intent.getStringExtra(EXTRA_REMOTE_PATH) ?: return START_NOT_STICKY
                val localPath = intent.getStringExtra(EXTRA_LOCAL_PATH) ?: return START_NOT_STICKY
                val transferId = intent.getLongExtra(EXTRA_TRANSFER_ID, -1L)
                if (transferId == -1L) return START_NOT_STICKY
                startDownload(transferId, remotePath, localPath)
            }
            ACTION_PAUSE -> pauseTransfer(intent.getLongExtra(EXTRA_TRANSFER_ID, -1L))
            ACTION_CANCEL -> cancelTransfer(intent.getLongExtra(EXTRA_TRANSFER_ID, -1L))
        }
        return START_NOT_STICKY
    }

    private fun startUpload(transferId: Long, localPath: String, remotePath: String) {
        startForeground(NOTIFICATION_ID, createNotification("Uploading..."))

        val job = serviceScope.launch {
            try {
                transferDao.updateTransferStatus(transferId, "IN_PROGRESS")
                
                var lastUpdateTime = 0L
                ftpClientManager.setProgressListener(object : com.ftpdroid.app.data.network.ftp.TransferProgressListener {
                    override fun onProgress(bytesTransferred: Long, totalBytes: Long) {
                        val now = System.currentTimeMillis()
                        if (now - lastUpdateTime > 500) { // Throttled update
                            val speed = if (lastUpdateTime > 0) {
                                (bytesTransferred * 1000) / (now - lastUpdateTime) // Simple speed estimate
                            } else 0L
                            serviceScope.launch {
                                transferDao.updateTransferProgress(transferId, bytesTransferred, 0) // DAO doesn't take speed in this version? checking
                            }
                            lastUpdateTime = now
                        }
                    }
                    override fun onComplete(success: Boolean) {}
                    override fun onError(message: String) {}
                })

                ftpClientManager.uploadFile(java.io.File(localPath), remotePath)
                transferDao.updateTransferStatus(transferId, "COMPLETED")
            } catch (e: Exception) {
                transferDao.updateTransferStatus(transferId, "FAILED")
            }
            checkAndStopService()
        }
        activeJobs[transferId] = job
    }

    private fun startDownload(transferId: Long, remotePath: String, localPath: String) {
        startForeground(NOTIFICATION_ID, createNotification("Downloading..."))

        val job = serviceScope.launch {
            try {
                transferDao.updateTransferStatus(transferId, "IN_PROGRESS")

                var lastUpdateTime = 0L
                ftpClientManager.setProgressListener(object : com.ftpdroid.app.data.network.ftp.TransferProgressListener {
                    override fun onProgress(bytesTransferred: Long, totalBytes: Long) {
                        val now = System.currentTimeMillis()
                        if (now - lastUpdateTime > 500) {
                            serviceScope.launch {
                                transferDao.updateTransferProgress(transferId, bytesTransferred, 0)
                            }
                            lastUpdateTime = now
                        }
                    }
                    override fun onComplete(success: Boolean) {}
                    override fun onError(message: String) {}
                })

                ftpClientManager.downloadFile(remotePath, java.io.File(localPath))
                transferDao.updateTransferStatus(transferId, "COMPLETED")
            } catch (e: Exception) {
                transferDao.updateTransferStatus(transferId, "FAILED")
            }
            checkAndStopService()
        }
        activeJobs[transferId] = job
    }

    private fun pauseTransfer(transferId: Long) {
        if (transferId == -1L) return
        activeJobs[transferId]?.cancel()
        activeJobs.remove(transferId)
        serviceScope.launch {
            transferDao.updateTransferStatus(transferId, "PAUSED")
        }
        checkAndStopService()
    }

    private fun cancelTransfer(transferId: Long) {
        if (transferId == -1L) return
        activeJobs[transferId]?.cancel()
        activeJobs.remove(transferId)
        serviceScope.launch {
            transferDao.updateTransferStatus(transferId, "CANCELLED")
        }
        checkAndStopService()
    }

    private fun checkAndStopService() {
        if (activeJobs.isEmpty()) {
            stopForeground(STOP_FOREGROUND_REMOVE)
            stopSelf()
        }
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                "Transfers",
                NotificationManager.IMPORTANCE_LOW
            ).apply {
                description = "File transfer notifications"
            }
            val manager = getSystemService(NotificationManager::class.java)
            manager.createNotificationChannel(channel)
        }
    }

    private fun createNotification(text: String): android.app.Notification {
        val mainIntent = Intent(this, MainActivity::class.java)
        val mainPending = PendingIntent.getActivity(
            this, 0, mainIntent, PendingIntent.FLAG_IMMUTABLE
        )

        return NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle("FTPDroid")
            .setContentText(text)
            .setSmallIcon(R.drawable.ic_download)
            .setContentIntent(mainPending)
            .setOngoing(true)
            .build()
    }

    override fun onBind(intent: Intent?): IBinder? = null

    companion object {
        const val ACTION_START_UPLOAD = "com.ftpdroid.app.ACTION_START_UPLOAD"
        const val ACTION_START_DOWNLOAD = "com.ftpdroid.app.ACTION_START_DOWNLOAD"
        const val ACTION_PAUSE = "com.ftpdroid.app.ACTION_PAUSE"
        const val ACTION_CANCEL = "com.ftpdroid.app.ACTION_CANCEL"
        const val EXTRA_TRANSFER_ID = "transfer_id"
        const val EXTRA_REMOTE_PATH = "remote_path"
        const val EXTRA_LOCAL_PATH = "local_path"
        const val NOTIFICATION_ID = 2
        const val CHANNEL_ID = "transfer_channel"
    }
}