package com.ftpdroid.app.ui.screen.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ftpdroid.app.data.network.ftp.FtpServerManager
import com.ftpdroid.app.domain.model.TransferStatus
import com.ftpdroid.app.domain.repository.ProfileRepository
import com.ftpdroid.app.domain.repository.ServerRepository
import com.ftpdroid.app.domain.repository.TransferRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.launch
import javax.inject.Inject

data class HomeUiState(
    val isServerRunning: Boolean = false,
    val activeProfilesCount: Int = 0,
    val activeTransfersCount: Int = 0,
    val recentActivity: List<ActivityItem> = emptyList()
)

data class ActivityItem(
    val id: Long,
    val title: String,
    val subtitle: String,
    val timestamp: Long
)

sealed interface HomeIntent {
    data object StartServer : HomeIntent
    data object StopServer : HomeIntent
    data object Refresh : HomeIntent
}

sealed interface HomeUiEvent {
    data class ShowError(val message: String) : HomeUiEvent
}

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val serverRepository: ServerRepository,
    private val profileRepository: ProfileRepository,
    private val transferRepository: TransferRepository,
    private val ftpServerManager: FtpServerManager
) : ViewModel() {
    private val _uiState = MutableStateFlow(HomeUiState())
    val uiState: StateFlow<HomeUiState> = _uiState.asStateFlow()

    init {
        observeData()
    }

    private fun observeData() {
        viewModelScope.launch {
            combine(
                ftpServerManager.state,
                profileRepository.getAllProfiles(),
                transferRepository.getAllTransfers()
            ) { serverState, profiles, transfers ->
                HomeUiState(
                    isServerRunning = serverState is FtpServerManager.ServerState.Running,
                    activeProfilesCount = profiles.size,
                    activeTransfersCount = transfers.count { it.status == TransferStatus.IN_PROGRESS },
                    recentActivity = emptyList() // TODO: Implement recent activity
                )
            }.collect { newState ->
                _uiState.value = newState
            }
        }
    }

    fun onIntent(intent: HomeIntent) {
        when (intent) {
            is HomeIntent.StartServer -> startServer()
            is HomeIntent.StopServer -> stopServer()
            is HomeIntent.Refresh -> refresh()
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