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

data class TransferHistoryUiState(
    val transfers: List<TransferHistoryItem> = emptyList(),
    val isLoading: Boolean = false,
    val filterStatus: HistoryFilter = HistoryFilter.ALL
)

data class TransferHistoryItem(
    val id: Long,
    val fileName: String,
    val filePath: String,
    val fileSize: Long,
    val direction: TransferDirection,
    val status: TransferStatus,
    val startTime: Long,
    val endTime: Long,
    val profileName: String,
    val errorMessage: String? = null
)

enum class HistoryFilter {
    ALL, COMPLETED, FAILED
}

sealed interface TransferHistoryIntent {
    data object LoadHistory : TransferHistoryIntent
    data object ClearHistory : TransferHistoryIntent
    data class SetFilter(val filter: HistoryFilter) : TransferHistoryIntent
    data class DeleteHistoryItem(val id: Long) : TransferHistoryIntent
}

sealed interface TransferHistoryUiEvent {
    data class ShowError(val message: String) : TransferHistoryUiEvent
}

@HiltViewModel
class TransferHistoryViewModel @Inject constructor(
    private val transferRepository: TransferRepository
) : ViewModel() {
    private val _uiState = MutableStateFlow(TransferHistoryUiState())
    val uiState: StateFlow<TransferHistoryUiState> = _uiState.asStateFlow()

    init {
        loadHistory()
    }

    fun onIntent(intent: TransferHistoryIntent) {
        when (intent) {
            is TransferHistoryIntent.LoadHistory -> loadHistory()
            is TransferHistoryIntent.ClearHistory -> clearHistory()
            is TransferHistoryIntent.SetFilter -> _uiState.value = _uiState.value.copy(filterStatus = intent.filter)
            is TransferHistoryIntent.DeleteHistoryItem -> deleteHistoryItem(intent.id)
        }
    }

    private fun loadHistory() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)
            transferRepository.getAllTransfers().collectLatest { transfers ->
                val items = transfers
                    .filter { it.status == DomainTransferStatus.COMPLETED || 
                             it.status == DomainTransferStatus.FAILED ||
                             it.status == DomainTransferStatus.CANCELLED }
                    .map { transfer ->
                        TransferHistoryItem(
                            id = transfer.id,
                            fileName = transfer.remotePath.substringAfterLast('/'),
                            filePath = transfer.localPath,
                            fileSize = transfer.totalBytes,
                            direction = if (transfer.type == TransferType.UPLOAD) 
                                TransferDirection.UPLOAD else TransferDirection.DOWNLOAD,
                            status = when (transfer.status) {
                                DomainTransferStatus.COMPLETED -> TransferStatus.COMPLETED
                                DomainTransferStatus.FAILED -> TransferStatus.FAILED
                                DomainTransferStatus.CANCELLED -> TransferStatus.FAILED
                                else -> TransferStatus.FAILED // Should not happen due to filter
                            },
                            startTime = transfer.startedAt,
                            endTime = transfer.completedAt ?: 0L,
                            profileName = "Profile ${transfer.profileId}",
                            errorMessage = transfer.errorMessage
                        )
                    }
                _uiState.value = _uiState.value.copy(
                    transfers = items,
                    isLoading = false
                )
            }
        }
    }

    private fun clearHistory() {
        viewModelScope.launch {
            transferRepository.clearOldTransfers(0)
        }
    }

    private fun deleteHistoryItem(id: Long) {
        viewModelScope.launch {
            transferRepository.deleteTransfer(id)
        }
    }
}