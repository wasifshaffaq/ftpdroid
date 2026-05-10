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

import kotlinx.coroutines.flow.first
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock

@AndroidEntryPoint
class TransferService : Service() {

    @Inject
    lateinit var transferDao: TransferDao

    @Inject
    lateinit var ftpClientManager: FtpClientManager

    private val serviceScope = CoroutineScope(Dispatchers.IO + Job())
    private var queueJob: Job? = null
    private val queueMutex = Mutex()
    private var isProcessing = false

    override fun onCreate() {
        super.onCreate()
        createNotificationChannel()
        startQueueProcessor()
    }

    private fun startQueueProcessor() {
        if (queueJob?.isActive == true) return
        queueJob = serviceScope.launch {
            // Observe pending transfers. When list changes, try to process.
            transferDao.getPendingTransfers().collect { pendingTransfers ->
                if (!isProcessing && pendingTransfers.any { it.status == "QUEUED" || it.status == "IN_PROGRESS" }) {
                    processQueueSequentially()
                }
            }
        }
    }

    private suspend fun processQueueSequentially() {
        queueMutex.withLock {
            if (isProcessing) return
            isProcessing = true
            try {
                // Keep processing as long as there are pending transfers
                while (true) {
                    val nextTransfer = transferDao.getPendingTransfers().first().firstOrNull { 
                        it.status == "QUEUED" || it.status == "IN_PROGRESS" 
                    } ?: break

                    // Ensure status is IN_PROGRESS before we start
                    if (nextTransfer.status == "QUEUED") {
                        transferDao.updateTransferStatus(nextTransfer.id, "IN_PROGRESS")
                    }

                    executeTransfer(nextTransfer)
                }
            } finally {
                isProcessing = false
                checkAndStopService()
            }
        }
    }

    private suspend fun executeTransfer(transfer: com.ftpdroid.app.data.local.db.entity.TransferEntity) {
        val transferId = transfer.id
        val actionText = if (transfer.type == "UPLOAD") "Uploading" else "Downloading"
        startForeground(NOTIFICATION_ID, createNotification("$actionText ${transfer.remotePath.substringAfterLast('/')}..."))

        try {
            var lastUpdateTime = System.currentTimeMillis()
            var lastBytesTransferred = 0L
            ftpClientManager.setProgressListener(object : com.ftpdroid.app.data.network.ftp.TransferProgressListener {
                override fun onProgress(bytesTransferred: Long, totalBytes: Long) {
                    val now = System.currentTimeMillis()
                    val deltaTime = now - lastUpdateTime
                    if (deltaTime > 1000) {
                        val speed = (bytesTransferred - lastBytesTransferred) * 1000 / deltaTime
                        serviceScope.launch {
                            transferDao.updateTransferProgress(transferId, bytesTransferred, speed)
                        }
                        lastUpdateTime = now
                        lastBytesTransferred = bytesTransferred
                    }
                }
                override fun onComplete(success: Boolean) {}
                override fun onError(message: String) {}
            })

            if (transfer.type == "UPLOAD") {
                ftpClientManager.uploadFile(java.io.File(transfer.localPath), transfer.remotePath)
            } else {
                ftpClientManager.downloadFile(transfer.remotePath, java.io.File(transfer.localPath))
            }
            transferDao.updateTransferStatus(transferId, "COMPLETED")
        } catch (e: Exception) {
            transferDao.updateTransferStatus(transferId, "FAILED")
        }
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        when (intent?.action) {
            ACTION_START_UPLOAD, ACTION_START_DOWNLOAD -> startQueueProcessor()
            ACTION_PAUSE -> pauseTransfer(intent.getLongExtra(EXTRA_TRANSFER_ID, -1L))
            ACTION_CANCEL -> cancelTransfer(intent.getLongExtra(EXTRA_TRANSFER_ID, -1L))
        }
        return START_NOT_STICKY
    }

    private fun pauseTransfer(transferId: Long) {
        if (transferId == -1L) return
        serviceScope.launch {
            transferDao.updateTransferStatus(transferId, "PAUSED")
            // The current job will finish/fail and the loop will pick up the next one
            // We should ideally disconnect the client to abort the current operation
            ftpClientManager.disconnect()
        }
    }

    private fun cancelTransfer(transferId: Long) {
        if (transferId == -1L) return
        serviceScope.launch {
            transferDao.updateTransferStatus(transferId, "CANCELLED")
            ftpClientManager.disconnect()
        }
    }

    private fun checkAndStopService() {
        serviceScope.launch {
            val pendingCount = transferDao.getPendingTransfers().first().size
            if (pendingCount == 0 && !isProcessing) {
                stopForeground(STOP_FOREGROUND_REMOVE)
                stopSelf()
            }
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