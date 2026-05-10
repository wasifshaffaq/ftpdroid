package com.ftpdroid.app.ui.screen.server

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ftpdroid.app.data.local.db.dao.ConnectionLogDao
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import javax.inject.Inject

data class ConnectionLogUiState(
    val logs: List<ConnectionLogEntry> = emptyList(),
    val isLoading: Boolean = false,
    val filterType: LogFilterType = LogFilterType.ALL
)

data class ConnectionLogEntry(
    val id: Long,
    val timestamp: Long,
    val ipAddress: String,
    val username: String?,
    val action: LogAction,
    val details: String
)

enum class LogAction {
    CONNECTED, DISCONNECTED, AUTH_FAILED, UPLOAD, DOWNLOAD, LIST, DELETE
}

enum class LogFilterType {
    ALL, CONNECTIONS, TRANSFERS, ERRORS
}

sealed interface ConnectionLogIntent {
    data object LoadLogs : ConnectionLogIntent
    data object ClearLogs : ConnectionLogIntent
    data class SetFilter(val filter: LogFilterType) : ConnectionLogIntent
}

sealed interface ConnectionLogUiEvent {
    data class ShowError(val message: String) : ConnectionLogUiEvent
}

@HiltViewModel
class ConnectionLogViewModel @Inject constructor(
    private val connectionLogDao: ConnectionLogDao
) : ViewModel() {
    private val _uiState = MutableStateFlow(ConnectionLogUiState())
    val uiState: StateFlow<ConnectionLogUiState> = _uiState.asStateFlow()

    init {
        loadLogs()
    }

    fun onIntent(intent: ConnectionLogIntent) {
        when (intent) {
            is ConnectionLogIntent.LoadLogs -> loadLogs()
            is ConnectionLogIntent.ClearLogs -> clearLogs()
            is ConnectionLogIntent.SetFilter -> {
                _uiState.value = _uiState.value.copy(filterType = intent.filter)
                loadLogs()
            }
        }
    }

    private fun loadLogs() {
        _uiState.value = _uiState.value.copy(isLoading = true)
        viewModelScope.launch {
            connectionLogDao.getAllLogs().collectLatest { entities ->
                val logs = entities.map { entity ->
                    ConnectionLogEntry(
                        id = entity.id,
                        timestamp = entity.timestamp,
                        ipAddress = entity.ipAddress,
                        username = entity.username,
                        action = when (entity.eventType) {
                            "CONNECT" -> LogAction.CONNECTED
                            "DISCONNECT" -> LogAction.DISCONNECTED
                            "AUTH_FAILED" -> LogAction.AUTH_FAILED
                            "UPLOAD" -> LogAction.UPLOAD
                            "DOWNLOAD" -> LogAction.DOWNLOAD
                            "DELETE" -> LogAction.DELETE
                            else -> LogAction.LIST
                        },
                        details = entity.message
                    )
                }
                
                val filteredLogs = when (_uiState.value.filterType) {
                    LogFilterType.ALL -> logs
                    LogFilterType.CONNECTIONS -> logs.filter { it.action == LogAction.CONNECTED || it.action == LogAction.DISCONNECTED }
                    LogFilterType.TRANSFERS -> logs.filter { it.action == LogAction.UPLOAD || it.action == LogAction.DOWNLOAD }
                    LogFilterType.ERRORS -> logs.filter { it.action == LogAction.AUTH_FAILED }
                }

                _uiState.value = _uiState.value.copy(
                    logs = filteredLogs,
                    isLoading = false
                )
            }
        }
    }

    private fun clearLogs() {
        viewModelScope.launch {
            connectionLogDao.clearAllLogs()
        }
    }
}