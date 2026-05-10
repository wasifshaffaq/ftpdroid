package com.ftpdroid.app.data.local.prefs

import android.content.Context
import android.content.SharedPreferences
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.onStart

enum class AppTheme(val displayName: String) {
    SYSTEM("Follow system"),
    LIGHT("Light"),
    DARK("Dark")
}

@Singleton
class AppPreferences @Inject constructor(
    @ApplicationContext context: Context
) {
    private val prefs: SharedPreferences = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    val appThemeFlow: Flow<AppTheme> = callbackFlow {
        val listener = SharedPreferences.OnSharedPreferenceChangeListener { _, key ->
            if (key == KEY_APP_THEME) {
                trySend(appTheme)
            }
        }
        prefs.registerOnSharedPreferenceChangeListener(listener)
        awaitClose {
            prefs.unregisterOnSharedPreferenceChangeListener(listener)
        }
    }.onStart {
        emit(appTheme)
    }

    var lastUsedProfileId: Long
        get() = prefs.getLong(KEY_LAST_USED_PROFILE_ID, -1L)
        set(value) = prefs.edit().putLong(KEY_LAST_USED_PROFILE_ID, value).apply()

    var lastRemotePath: String
        get() = prefs.getString(KEY_LAST_REMOTE_PATH, "/") ?: "/"
        set(value) = prefs.edit().putString(KEY_LAST_REMOTE_PATH, value).apply()

    var appTheme: AppTheme
        get() = try {
            AppTheme.valueOf(prefs.getString(KEY_APP_THEME, AppTheme.SYSTEM.name) ?: AppTheme.SYSTEM.name)
        } catch (e: Exception) {
            AppTheme.SYSTEM
        }
        set(value) = prefs.edit().putString(KEY_APP_THEME, value.name).apply()

    var showHiddenFiles: Boolean
        get() = prefs.getBoolean(KEY_SHOW_HIDDEN_FILES, false)
        set(value) = prefs.edit().putBoolean(KEY_SHOW_HIDDEN_FILES, value).apply()

    var sortOrder: String
        get() = prefs.getString(KEY_SORT_ORDER, SORT_NAME_ASC) ?: SORT_NAME_ASC
        set(value) = prefs.edit().putString(KEY_SORT_ORDER, value).apply()

    var transferQueueEnabled: Boolean
        get() = prefs.getBoolean(KEY_TRANSFER_QUEUE_ENABLED, true)
        set(value) = prefs.edit().putBoolean(KEY_TRANSFER_QUEUE_ENABLED, value).apply()

    var maxConcurrentTransfers: Int
        get() = prefs.getInt(KEY_MAX_CONCURRENT_TRANSFERS, 2)
        set(value) = prefs.edit().putInt(KEY_MAX_CONCURRENT_TRANSFERS, value).apply()

    var keepScreenOn: Boolean
        get() = prefs.getBoolean(KEY_KEEP_SCREEN_ON, false)
        set(value) = prefs.edit().putBoolean(KEY_KEEP_SCREEN_ON, value).apply()

    fun clearAll() {
        prefs.edit().clear().apply()
    }

    companion object {
        private const val PREFS_NAME = "ftpdroid_app_prefs"
        private const val KEY_LAST_USED_PROFILE_ID = "last_used_profile_id"
        private const val KEY_LAST_REMOTE_PATH = "last_remote_path"
        private const val KEY_APP_THEME = "app_theme"
        private const val KEY_SHOW_HIDDEN_FILES = "show_hidden_files"
        private const val KEY_SORT_ORDER = "sort_order"
        private const val KEY_TRANSFER_QUEUE_ENABLED = "transfer_queue_enabled"
        private const val KEY_MAX_CONCURRENT_TRANSFERS = "max_concurrent_transfers"
        private const val KEY_KEEP_SCREEN_ON = "keep_screen_on"

        const val SORT_NAME_ASC = "name_asc"
        const val SORT_NAME_DESC = "name_desc"
        const val SORT_DATE_ASC = "date_asc"
        const val SORT_DATE_DESC = "date_desc"
        const val SORT_SIZE_ASC = "size_asc"
        const val SORT_SIZE_DESC = "size_desc"
    }
}