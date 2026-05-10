package com.ftpdroid.app.ui.screen.server

import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

data class ServerSettingsUiState(
    val port: Int = 2121,
    val maxConnections: Int = 10,
    val welcomeMessage: String = "Welcome to FTPDroid",
    val enableAnonymous: Boolean = false,
    val enablePassiveMode: Boolean = true,
    val passivePortRange: String = "50000-50100",
    val allowedIpRanges: String = "",
    val enableLogging: Boolean = true,
    val isLoading: Boolean = false,
    val showSaveSuccess: Boolean = false
)

sealed interface ServerSettingsIntent {
    data class UpdatePort(val port: Int) : ServerSettingsIntent
    data class UpdateMaxConnections(val max: Int) : ServerSettingsIntent
    data class UpdateWelcomeMessage(val message: String) : ServerSettingsIntent
    data class ToggleAnonymous(val enabled: Boolean) : ServerSettingsIntent
    data class TogglePassiveMode(val enabled: Boolean) : ServerSettingsIntent
    data class UpdatePassivePortRange(val range: String) : ServerSettingsIntent
    data class UpdateAllowedIpRanges(val ranges: String) : ServerSettingsIntent
    data class ToggleLogging(val enabled: Boolean) : ServerSettingsIntent
    data object Save : ServerSettingsIntent
}

sealed interface ServerSettingsUiEvent {
    data class ShowError(val message: String) : ServerSettingsUiEvent
}

class ServerSettingsViewModel : ViewModel() {
    private val _uiState = MutableStateFlow(ServerSettingsUiState())
    val uiState: StateFlow<ServerSettingsUiState> = _uiState.asStateFlow()

    fun onIntent(intent: ServerSettingsIntent) {
        when (intent) {
            is ServerSettingsIntent.UpdatePort -> _uiState.value = _uiState.value.copy(port = intent.port)
            is ServerSettingsIntent.UpdateMaxConnections -> _uiState.value = _uiState.value.copy(maxConnections = intent.max)
            is ServerSettingsIntent.UpdateWelcomeMessage -> _uiState.value = _uiState.value.copy(welcomeMessage = intent.message)
            is ServerSettingsIntent.ToggleAnonymous -> _uiState.value = _uiState.value.copy(enableAnonymous = intent.enabled)
            is ServerSettingsIntent.TogglePassiveMode -> _uiState.value = _uiState.value.copy(enablePassiveMode = intent.enabled)
            is ServerSettingsIntent.UpdatePassivePortRange -> _uiState.value = _uiState.value.copy(passivePortRange = intent.range)
            is ServerSettingsIntent.UpdateAllowedIpRanges -> _uiState.value = _uiState.value.copy(allowedIpRanges = intent.ranges)
            is ServerSettingsIntent.ToggleLogging -> _uiState.value = _uiState.value.copy(enableLogging = intent.enabled)
            is ServerSettingsIntent.Save -> saveSettings()
        }
    }

    private fun saveSettings() {
        _uiState.value = _uiState.value.copy(isLoading = true, showSaveSuccess = false)
        // TODO: Implement save logic
        _uiState.value = _uiState.value.copy(isLoading = false, showSaveSuccess = true)
    }
}