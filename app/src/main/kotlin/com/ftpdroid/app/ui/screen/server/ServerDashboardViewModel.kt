package com.ftpdroid.app.ui.screen.server

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ftpdroid.app.data.network.ftp.FtpServerManager
import com.ftpdroid.app.domain.repository.ServerRepository
import com.ftpdroid.app.domain.repository.TransferRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.launch
import java.net.Inet4Address
import java.net.NetworkInterface
import javax.inject.Inject

data class ServerDashboardUiState(
    val isRunning: Boolean = false,
    val serverAddress: String = "",
    val port: Int = 2121,
    val activeConnections: Int = 0,
    val totalBytesTransferred: Long = 0L,
    val uptimeSeconds: Long = 0L,
    val errorMessage: String? = null
)

sealed interface ServerDashboardIntent {
    data object StartServer : ServerDashboardIntent
    data object StopServer : ServerDashboardIntent
    data object Refresh : ServerDashboardIntent
}

@HiltViewModel
class ServerDashboardViewModel @Inject constructor(
    private val serverRepository: ServerRepository,
    private val transferRepository: TransferRepository,
    private val ftpServerManager: FtpServerManager
) : ViewModel() {
    private val _uiState = MutableStateFlow(ServerDashboardUiState())
    val uiState: StateFlow<ServerDashboardUiState> = _uiState.asStateFlow()

    init {
        observeData()
        startUptimeTimer()
    }

    private fun observeData() {
        viewModelScope.launch {
            combine(
                ftpServerManager.state,
                transferRepository.getAllTransfers()
            ) { serverState, transfers ->
                val totalBytes = transfers
                    .filter { it.profileId == -1L } // Only server-side transfers
                    .sumOf { it.transferredBytes }

                when (serverState) {
                    is FtpServerManager.ServerState.Running -> {
                        ServerDashboardUiState(
                            isRunning = true,
                            serverAddress = serverState.ipAddress,
                            port = serverState.port,
                            activeConnections = serverState.connectedClients,
                            totalBytesTransferred = totalBytes,
                            uptimeSeconds = (System.currentTimeMillis() - ftpServerManager.startTime) / 1000,
                            errorMessage = null
                        )
                    }
                    is FtpServerManager.ServerState.Stopped -> {
                        ServerDashboardUiState(
                            isRunning = false,
                            totalBytesTransferred = totalBytes,
                            errorMessage = null
                        )
                    }
                    is FtpServerManager.ServerState.Error -> {
                        ServerDashboardUiState(
                            isRunning = false,
                            totalBytesTransferred = totalBytes,
                            errorMessage = serverState.message
                        )
                    }
                    is FtpServerManager.ServerState.Starting -> {
                        ServerDashboardUiState(
                            isRunning = false,
                            port = serverState.port,
                            totalBytesTransferred = totalBytes
                        )
                    }
                }
            }.collect {
                _uiState.value = it
            }
        }
    }

    private fun startUptimeTimer() {
        viewModelScope.launch {
            while (true) {
                if (_uiState.value.isRunning) {
                    val uptime = (System.currentTimeMillis() - ftpServerManager.startTime) / 1000
                    _uiState.value = _uiState.value.copy(uptimeSeconds = uptime)
                }
                delay(1000)
            }
        }
    }

    fun onIntent(intent: ServerDashboardIntent) {
        when (intent) {
            is ServerDashboardIntent.StartServer -> startServer()
            is ServerDashboardIntent.StopServer -> stopServer()
            is ServerDashboardIntent.Refresh -> refresh()
        }
    }

    private fun startServer() {
        viewModelScope.launch {
            val config = serverRepository.getServerConfig()
            serverRepository.startServer(config)
        }
    }

    private fun stopServer() {
        viewModelScope.launch {
            serverRepository.stopServer()
        }
    }

    private fun refresh() {
        observeData()
    }
}