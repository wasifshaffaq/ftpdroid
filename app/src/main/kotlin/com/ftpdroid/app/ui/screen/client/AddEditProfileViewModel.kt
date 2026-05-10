package com.ftpdroid.app.ui.screen.client

import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

data class AddEditProfileUiState(
    val profileId: Long? = null,
    val name: String = "",
    val host: String = "",
    val port: String = "21",
    val username: String = "",
    val password: String = "",
    val remotePath: String = "/",
    val useSsl: Boolean = false,
    val isLoading: Boolean = false,
    val isSaving: Boolean = false,
    val showSaveSuccess: Boolean = false
)

sealed interface AddEditProfileIntent {
    data class LoadProfile(val profileId: Long) : AddEditProfileIntent
    data class UpdateName(val name: String) : AddEditProfileIntent
    data class UpdateHost(val host: String) : AddEditProfileIntent
    data class UpdatePort(val port: String) : AddEditProfileIntent
    data class UpdateUsername(val username: String) : AddEditProfileIntent
    data class UpdatePassword(val password: String) : AddEditProfileIntent
    data class UpdateRemotePath(val path: String) : AddEditProfileIntent
    data class ToggleSsl(val useSsl: Boolean) : AddEditProfileIntent
    data object Save : AddEditProfileIntent
}

sealed interface AddEditProfileUiEvent {
    data class ShowError(val message: String) : AddEditProfileUiEvent
}

class AddEditProfileViewModel : ViewModel() {
    private val _uiState = MutableStateFlow(AddEditProfileUiState())
    val uiState: StateFlow<AddEditProfileUiState> = _uiState.asStateFlow()

    fun onIntent(intent: AddEditProfileIntent) {
        when (intent) {
            is AddEditProfileIntent.LoadProfile -> loadProfile(intent.profileId)
            is AddEditProfileIntent.UpdateName -> _uiState.value = _uiState.value.copy(name = intent.name)
            is AddEditProfileIntent.UpdateHost -> _uiState.value = _uiState.value.copy(host = intent.host)
            is AddEditProfileIntent.UpdatePort -> _uiState.value = _uiState.value.copy(port = intent.port)
            is AddEditProfileIntent.UpdateUsername -> _uiState.value = _uiState.value.copy(username = intent.username)
            is AddEditProfileIntent.UpdatePassword -> _uiState.value = _uiState.value.copy(password = intent.password)
            is AddEditProfileIntent.UpdateRemotePath -> _uiState.value = _uiState.value.copy(remotePath = intent.path)
            is AddEditProfileIntent.ToggleSsl -> _uiState.value = _uiState.value.copy(useSsl = intent.useSsl)
            is AddEditProfileIntent.Save -> saveProfile()
        }
    }

    private fun loadProfile(profileId: Long) {
        _uiState.value = _uiState.value.copy(isLoading = true, profileId = profileId)
        // TODO: Load profile from repository
        _uiState.value = _uiState.value.copy(isLoading = false)
    }

    private fun saveProfile() {
        _uiState.value = _uiState.value.copy(isSaving = true, showSaveSuccess = false)
        // TODO: Save profile
        _uiState.value = _uiState.value.copy(isSaving = false, showSaveSuccess = true)
    }
}