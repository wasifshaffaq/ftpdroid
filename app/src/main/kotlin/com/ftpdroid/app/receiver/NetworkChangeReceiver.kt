package com.ftpdroid.app.receiver

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.net.wifi.WifiManager
import com.ftpdroid.app.service.FtpServerService
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class NetworkChangeReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        val prefs = context.getSharedPreferences("ftp_prefs", Context.MODE_PRIVATE)
        val stopOnDisconnect = prefs.getBoolean("stop_on_wifi_disconnect", false)
        val startOnWifiConnect = prefs.getBoolean("start_on_wifi_connect", false)

        val wifiState = intent.getIntExtra(WifiManager.EXTRA_WIFI_STATE, -1)

        when (wifiState) {
            WifiManager.WIFI_STATE_DISABLED -> {
                if (stopOnDisconnect) {
                    val serviceIntent = Intent(context, FtpServerService::class.java).apply {
                        action = FtpServerService.ACTION_STOP
                    }
                    context.startService(serviceIntent)
                }
            }
            WifiManager.WIFI_STATE_ENABLED -> {
                if (startOnWifiConnect) {
                    val serviceIntent = Intent(context, FtpServerService::class.java).apply {
                        action = FtpServerService.ACTION_START
                    }
                    context.startForegroundService(serviceIntent)
                }
            }
        }
    }
}