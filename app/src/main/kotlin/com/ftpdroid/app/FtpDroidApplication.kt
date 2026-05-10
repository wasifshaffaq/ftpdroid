package com.ftpdroid.app

import android.app.Application
import android.app.NotificationChannel
import android.app.NotificationManager
import android.os.Build
import dagger.hilt.android.HiltAndroidApp

@HiltAndroidApp
class FtpDroidApplication : Application() {

    override fun onCreate() {
        super.onCreate()
        createNotificationChannels()
    }

    private fun createNotificationChannels() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val notificationManager = getSystemService(NotificationManager::class.java)

            // FTP Server Channel
            val serverChannel = NotificationChannel(
                SERVER_CHANNEL_ID,
                "FTP Server",
                NotificationManager.IMPORTANCE_LOW
            ).apply {
                description = "FTP Server status notifications"
                setShowBadge(false)
            }

            // Transfer Channel
            val transferChannel = NotificationChannel(
                TRANSFER_CHANNEL_ID,
                "File Transfers",
                NotificationManager.IMPORTANCE_LOW
            ).apply {
                description = "File transfer progress notifications"
                setShowBadge(true)
            }

            notificationManager.createNotificationChannels(listOf(serverChannel, transferChannel))
        }
    }

    companion object {
        const val SERVER_CHANNEL_ID = "ftp_server_channel"
        const val TRANSFER_CHANNEL_ID = "transfer_channel"
    }
}