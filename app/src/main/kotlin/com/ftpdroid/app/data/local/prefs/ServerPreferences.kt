package com.ftpdroid.app.data.local.prefs

import android.content.Context
import android.content.SharedPreferences
import com.ftpdroid.app.domain.model.ServerConfig
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ServerPreferences @Inject constructor(
    @ApplicationContext context: Context
) {
    private val prefs: SharedPreferences = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
    private val json = Json { ignoreUnknownKeys = true }

    fun getServerConfig(): ServerConfig {
        val jsonString = prefs.getString(KEY_SERVER_CONFIG, null)
        return if (jsonString != null) {
            json.decodeFromString<ServerConfig>(jsonString)
        } else {
            ServerConfig()
        }
    }

    fun saveServerConfig(config: ServerConfig) {
        prefs.edit().putString(KEY_SERVER_CONFIG, json.encodeToString(config)).apply()
    }

    var isServerRunning: Boolean
        get() = prefs.getBoolean(KEY_SERVER_RUNNING, false)
        set(value) = prefs.edit().putBoolean(KEY_SERVER_RUNNING, value).apply()

    var serverStartTime: Long
        get() = prefs.getLong(KEY_SERVER_START_TIME, 0L)
        set(value) = prefs.edit().putLong(KEY_SERVER_START_TIME, value).apply()

    fun clearServerConfig() {
        prefs.edit().remove(KEY_SERVER_CONFIG).apply()
    }

    companion object {
        private const val PREFS_NAME = "ftpdroid_server_prefs"
        private const val KEY_SERVER_CONFIG = "server_config"
        private const val KEY_SERVER_RUNNING = "server_running"
        private const val KEY_SERVER_START_TIME = "server_start_time"
    }
}