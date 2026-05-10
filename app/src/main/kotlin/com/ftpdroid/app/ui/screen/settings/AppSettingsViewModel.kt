package com.ftpdroid.app.ui.screen.settings

import androidx.lifecycle.ViewModel
import com.ftpdroid.app.data.local.prefs.AppPreferences
import com.ftpdroid.app.data.local.prefs.AppTheme
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject

data class AppSettingsUiState(
    val theme: AppTheme = AppTheme.SYSTEM,
    val notificationsEnabled: Boolean = true,
    val autoStartServer: Boolean = false,
    val keepScreenOn: Boolean = false,
    val downloadPath: String = "",
    val maxConcurrentTransfers: Int = 3,
    val bufferSize: Int = 65536,
    val isLoading: Boolean = false,
    val showSaveSuccess: Boolean = false
)

sealed interface AppSettingsIntent {
    data class UpdateTheme(val theme: AppTheme) : AppSettingsIntent
    data class ToggleNotifications(val enabled: Boolean) : AppSettingsIntent
    data class ToggleAutoStartServer(val enabled: Boolean) : AppSettingsIntent
    data class ToggleKeepScreenOn(val enabled: Boolean) : AppSettingsIntent
    data class UpdateDownloadPath(val path: String) : AppSettingsIntent
    data class UpdateMaxConcurrentTransfers(val count: Int) : AppSettingsIntent
    data class UpdateBufferSize(val size: Int) : AppSettingsIntent
    data object Save : AppSettingsIntent
}

sealed interface AppSettingsUiEvent {
    data class ShowError(val message: String) : AppSettingsUiEvent
}

@HiltViewModel
class AppSettingsViewModel @Inject constructor(
    private val appPreferences: AppPreferences
) : ViewModel() {
    private val _uiState = MutableStateFlow(AppSettingsUiState())
    val uiState: StateFlow<AppSettingsUiState> = _uiState.asStateFlow()

    init {
        loadSettings()
    }

    private fun loadSettings() {
        _uiState.value = AppSettingsUiState(
            theme = appPreferences.appTheme,
            keepScreenOn = appPreferences.keepScreenOn,
            maxConcurrentTransfers = appPreferences.maxConcurrentTransfers
        )
    }

    fun onIntent(intent: AppSettingsIntent) {
        when (intent) {
            is AppSettingsIntent.UpdateTheme -> {
                _uiState.value = _uiState.value.copy(theme = intent.theme)
                appPreferences.appTheme = intent.theme
            }
            is AppSettingsIntent.ToggleNotifications -> _uiState.value = _uiState.value.copy(notificationsEnabled = intent.enabled)
            is AppSettingsIntent.ToggleAutoStartServer -> _uiState.value = _uiState.value.copy(autoStartServer = intent.enabled)
            is AppSettingsIntent.ToggleKeepScreenOn -> {
                _uiState.value = _uiState.value.copy(keepScreenOn = intent.enabled)
                appPreferences.keepScreenOn = intent.enabled
            }
            is AppSettingsIntent.UpdateDownloadPath -> _uiState.value = _uiState.value.copy(downloadPath = intent.path)
            is AppSettingsIntent.UpdateMaxConcurrentTransfers -> {
                _uiState.value = _uiState.value.copy(maxConcurrentTransfers = intent.count)
                appPreferences.maxConcurrentTransfers = intent.count
            }
            is AppSettingsIntent.UpdateBufferSize -> _uiState.value = _uiState.value.copy(bufferSize = intent.size)
            is AppSettingsIntent.Save -> saveSettings()
        }
    }

    private fun saveSettings() {
        _uiState.value = _uiState.value.copy(isLoading = true, showSaveSuccess = false)
        // TODO: Save settings
        _uiState.value = _uiState.value.copy(isLoading = false, showSaveSuccess = true)
    }
}