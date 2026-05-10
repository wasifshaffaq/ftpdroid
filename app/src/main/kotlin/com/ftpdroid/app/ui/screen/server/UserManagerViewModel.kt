package com.ftpdroid.app.ui.screen.server

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ftpdroid.app.domain.model.ServerUser
import com.ftpdroid.app.domain.repository.ServerRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import javax.inject.Inject

data class UserManagerUiState(
    val users: List<FtpUser> = emptyList(),
    val isLoading: Boolean = false,
    val showAddDialog: Boolean = false,
    val editingUser: FtpUser? = null
)

data class FtpUser(
    val id: Long = 0,
    val username: String,
    val password: String = "",
    val homeDirectory: String = "/",
    val maxConnections: Int = 1,
    val isEnabled: Boolean = true
)

sealed interface UserManagerIntent {
    data object LoadUsers : UserManagerIntent
    data object ShowAddDialog : UserManagerIntent
    data class EditUser(val user: FtpUser) : UserManagerIntent
    data class DeleteUser(val userId: Long) : UserManagerIntent
    data class SaveUser(val user: FtpUser) : UserManagerIntent
    data object HideDialog : UserManagerIntent
}

sealed interface UserManagerUiEvent {
    data class ShowError(val message: String) : UserManagerUiEvent
}

@HiltViewModel
class UserManagerViewModel @Inject constructor(
    private val serverRepository: ServerRepository
) : ViewModel() {
    private val _uiState = MutableStateFlow(UserManagerUiState())
    val uiState: StateFlow<UserManagerUiState> = _uiState.asStateFlow()

    init {
        loadUsers()
    }

    fun onIntent(intent: UserManagerIntent) {
        when (intent) {
            is UserManagerIntent.LoadUsers -> loadUsers()
            is UserManagerIntent.ShowAddDialog -> _uiState.value = _uiState.value.copy(showAddDialog = true, editingUser = null)
            is UserManagerIntent.EditUser -> _uiState.value = _uiState.value.copy(showAddDialog = true, editingUser = intent.user)
            is UserManagerIntent.DeleteUser -> deleteUser(intent.userId)
            is UserManagerIntent.SaveUser -> saveUser(intent.user)
            is UserManagerIntent.HideDialog -> _uiState.value = _uiState.value.copy(showAddDialog = false, editingUser = null)
        }
    }

    private fun loadUsers() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)
            serverRepository.getAllUsers().collectLatest { users ->
                val ftpUsers = users.map { user ->
                    FtpUser(
                        id = user.id,
                        username = user.username,
                        password = user.password,
                        homeDirectory = user.homeDirectory,
                        maxConnections = 1, // Default as it's not in ServerUser
                        isEnabled = true // Default as it's not in ServerUser
                    )
                }
                _uiState.value = _uiState.value.copy(
                    users = ftpUsers,
                    isLoading = false
                )
            }
        }
    }

    private fun deleteUser(userId: Long) {
        viewModelScope.launch {
            serverRepository.deleteUser(userId)
        }
    }

    private fun saveUser(user: FtpUser) {
        viewModelScope.launch {
            val serverUser = ServerUser(
                id = user.id,
                username = user.username,
                password = user.password,
                homeDirectory = user.homeDirectory,
                canRead = true,
                canWrite = true,
                canDelete = true
            )
            if (user.id == 0L) {
                serverRepository.addUser(serverUser)
            } else {
                serverRepository.updateUser(serverUser)
            }
            _uiState.value = _uiState.value.copy(showAddDialog = false, editingUser = null)
        }
    }
}