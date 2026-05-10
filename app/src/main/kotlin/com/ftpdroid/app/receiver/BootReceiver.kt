package com.ftpdroid.app.receiver

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.ftpdroid.app.service.FtpServerService
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class BootReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action == Intent.ACTION_BOOT_COMPLETED) {
            val prefs = context.getSharedPreferences("ftp_prefs", Context.MODE_PRIVATE)
            val autoStart = prefs.getBoolean("auto_start_on_boot", false)

            if (autoStart) {
                val serviceIntent = Intent(context, FtpServerService::class.java).apply {
                    action = FtpServerService.ACTION_START
                }
                context.startForegroundService(serviceIntent)
            }
        }
    }
}