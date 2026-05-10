package com.ftpdroid.app.ui.screen.client

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ftpdroid.app.domain.model.Protocol
import com.ftpdroid.app.domain.repository.ProfileRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import javax.inject.Inject

data class ProfileListUiState(
    val profiles: List<ConnectionProfile> = emptyList(),
    val isLoading: Boolean = false
)

data class ConnectionProfile(
    val id: Long = 0,
    val name: String,
    val host: String,
    val port: Int = 21,
    val username: String,
    val password: String = "",
    val remotePath: String = "/",
    val useSsl: Boolean = false,
    val lastConnected: Long? = null
)

sealed interface ProfileListIntent {
    data object LoadProfiles : ProfileListIntent
    data class DeleteProfile(val profileId: Long) : ProfileListIntent
    data class ConnectProfile(val profile: ConnectionProfile) : ProfileListIntent
}

sealed interface ProfileListUiEvent {
    data class ShowError(val message: String) : ProfileListUiEvent
    data class NavigateToFileBrowser(val profileId: Long) : ProfileListUiEvent
}

@HiltViewModel
class ProfileListViewModel @Inject constructor(
    private val profileRepository: ProfileRepository
) : ViewModel() {
    private val _uiState = MutableStateFlow(ProfileListUiState())
    val uiState: StateFlow<ProfileListUiState> = _uiState.asStateFlow()

    init {
        loadProfiles()
    }

    fun onIntent(intent: ProfileListIntent) {
        when (intent) {
            is ProfileListIntent.LoadProfiles -> loadProfiles()
            is ProfileListIntent.DeleteProfile -> deleteProfile(intent.profileId)
            is ProfileListIntent.ConnectProfile -> connectProfile(intent.profile)
        }
    }

    private fun loadProfiles() {
        _uiState.value = _uiState.value.copy(isLoading = true)
        viewModelScope.launch {
            profileRepository.getAllProfiles().collectLatest { profiles ->
                _uiState.value = _uiState.value.copy(
                    profiles = profiles.map { profile ->
                        ConnectionProfile(
                            id = profile.id,
                            name = profile.name,
                            host = profile.host,
                            port = profile.port,
                            username = profile.username,
                            password = profile.password,
                            remotePath = profile.initialRemotePath,
                            useSsl = profile.protocol != Protocol.FTP,
                            lastConnected = null // TODO: Add lastConnected to domain model
                        )
                    },
                    isLoading = false
                )
            }
        }
    }

    private fun deleteProfile(profileId: Long) {
        viewModelScope.launch {
            profileRepository.deleteProfile(profileId)
        }
    }

    private fun connectProfile(profile: ConnectionProfile) {
        // Handled by UI navigation
    }
}