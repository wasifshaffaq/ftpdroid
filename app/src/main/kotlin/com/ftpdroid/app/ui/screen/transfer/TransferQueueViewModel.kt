package com.ftpdroid.app.ui.screen.transfer

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ftpdroid.app.domain.model.TransferStatus as DomainTransferStatus
import com.ftpdroid.app.domain.model.TransferType
import com.ftpdroid.app.domain.repository.TransferRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import javax.inject.Inject

data class TransferQueueUiState(
    val activeTransfers: List<TransferItem> = emptyList(),
    val isLoading: Boolean = false
)

data class TransferItem(
    val id: Long,
    val fileName: String,
    val filePath: String,
    val fileSize: Long,
    val transferredBytes: Long = 0,
    val direction: TransferDirection = TransferDirection.DOWNLOAD,
    val status: TransferStatus = TransferStatus.PENDING,
    val speed: Long = 0,
    val startTime: Long = 0,
    val profileName: String = ""
)

enum class TransferDirection {
    DOWNLOAD, UPLOAD
}

enum class TransferStatus {
    PENDING, IN_PROGRESS, PAUSED, COMPLETED, FAILED
}

sealed interface TransferQueueIntent {
    data object LoadTransfers : TransferQueueIntent
    data class PauseTransfer(val transferId: Long) : TransferQueueIntent
    data class ResumeTransfer(val transferId: Long) : TransferQueueIntent
    data class CancelTransfer(val transferId: Long) : TransferQueueIntent
    data object PauseAll : TransferQueueIntent
    data object ResumeAll : TransferQueueIntent
}

sealed interface TransferQueueUiEvent {
    data class ShowError(val message: String) : TransferQueueUiEvent
}

@HiltViewModel
class TransferQueueViewModel @Inject constructor(
    private val transferRepository: TransferRepository
) : ViewModel() {
    private val _uiState = MutableStateFlow(TransferQueueUiState())
    val uiState: StateFlow<TransferQueueUiState> = _uiState.asStateFlow()

    init {
        loadTransfers()
    }

    fun onIntent(intent: TransferQueueIntent) {
        when (intent) {
            is TransferQueueIntent.LoadTransfers -> loadTransfers()
            is TransferQueueIntent.PauseTransfer -> pauseTransfer(intent.transferId)
            is TransferQueueIntent.ResumeTransfer -> resumeTransfer(intent.transferId)
            is TransferQueueIntent.CancelTransfer -> cancelTransfer(intent.transferId)
            is TransferQueueIntent.PauseAll -> pauseAll()
            is TransferQueueIntent.ResumeAll -> resumeAll()
        }
    }

    private fun loadTransfers() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)
            transferRepository.getPendingTransfers().collectLatest { transfers ->
                val items = transfers.map { transfer ->
                    TransferItem(
                        id = transfer.id,
                        fileName = transfer.remotePath.substringAfterLast('/'),
                        filePath = transfer.localPath,
                        fileSize = transfer.totalBytes,
                        transferredBytes = transfer.transferredBytes,
                        direction = if (transfer.type == TransferType.UPLOAD) 
                            TransferDirection.UPLOAD else TransferDirection.DOWNLOAD,
                        status = when (transfer.status) {
                            DomainTransferStatus.QUEUED -> TransferStatus.PENDING
                            DomainTransferStatus.IN_PROGRESS -> TransferStatus.IN_PROGRESS
                            DomainTransferStatus.PAUSED -> TransferStatus.PAUSED
                            DomainTransferStatus.COMPLETED -> TransferStatus.COMPLETED
                            DomainTransferStatus.FAILED -> TransferStatus.FAILED
                            DomainTransferStatus.CANCELLED -> TransferStatus.FAILED
                        },
                        speed = transfer.speedBytesPerSec,
                        startTime = transfer.startedAt,
                        profileName = "Profile ${transfer.profileId}" // In a real app we'd fetch the name
                    )
                }
                _uiState.value = _uiState.value.copy(
                    activeTransfers = items,
                    isLoading = false
                )
            }
        }
    }

    private fun pauseTransfer(transferId: Long) {
        viewModelScope.launch {
            transferRepository.updateTransferStatus(transferId, DomainTransferStatus.PAUSED)
        }
    }

    private fun resumeTransfer(transferId: Long) {
        viewModelScope.launch {
            transferRepository.updateTransferStatus(transferId, DomainTransferStatus.QUEUED)
        }
    }

    private fun cancelTransfer(transferId: Long) {
        viewModelScope.launch {
            transferRepository.deleteTransfer(transferId)
        }
    }

    private fun pauseAll() {
        // TODO: Implement pause all in repository if needed
    }

    private fun resumeAll() {
        // TODO: Implement resume all in repository if needed
    }
}