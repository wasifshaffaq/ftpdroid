package com.ftpdroid.app.ui.screen.client

import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

data class FileBrowserUiState(
    val profileId: Long = 0,
    val currentPath: String = "/",
    val files: List<FileItem> = emptyList(),
    val isLoading: Boolean = false,
    val isConnected: Boolean = false,
    val selectedFiles: Set<String> = emptySet(),
    val sortOrder: SortOrder = SortOrder.NAME_ASC,
    val showHiddenFiles: Boolean = false
)

data class FileItem(
    val name: String,
    val path: String,
    val isDirectory: Boolean,
    val size: Long = 0,
    val modifiedTime: Long = 0,
    val permissions: String = ""
)

enum class SortOrder {
    NAME_ASC, NAME_DESC, SIZE_ASC, SIZE_DESC, DATE_ASC, DATE_DESC
}

sealed interface FileBrowserIntent {
    data class LoadDirectory(val path: String) : FileBrowserIntent
    data class NavigateToPath(val path: String) : FileBrowserIntent
    data object NavigateUp : FileBrowserIntent
    data class ToggleSelection(val fileName: String) : FileBrowserIntent
    data object ClearSelection : FileBrowserIntent
    data class SetSortOrder(val order: SortOrder) : FileBrowserIntent
    data class ToggleHiddenFiles(val show: Boolean) : FileBrowserIntent
    data class DownloadFile(val file: FileItem) : FileBrowserIntent
    data class UploadFile(val localPath: String) : FileBrowserIntent
}

sealed interface FileBrowserUiEvent {
    data class ShowError(val message: String) : FileBrowserUiEvent
    data class NavigateToFileBrowser(val profileId: Long, val path: String) : FileBrowserUiEvent
}

class FileBrowserViewModel : ViewModel() {
    private val _uiState = MutableStateFlow(FileBrowserUiState())
    val uiState: StateFlow<FileBrowserUiState> = _uiState.asStateFlow()

    fun onIntent(intent: FileBrowserIntent) {
        when (intent) {
            is FileBrowserIntent.LoadDirectory -> loadDirectory(intent.path)
            is FileBrowserIntent.NavigateToPath -> loadDirectory(intent.path)
            is FileBrowserIntent.NavigateUp -> navigateUp()
            is FileBrowserIntent.ToggleSelection -> toggleSelection(intent.fileName)
            is FileBrowserIntent.ClearSelection -> _uiState.value = _uiState.value.copy(selectedFiles = emptySet())
            is FileBrowserIntent.SetSortOrder -> _uiState.value = _uiState.value.copy(sortOrder = intent.order)
            is FileBrowserIntent.ToggleHiddenFiles -> _uiState.value = _uiState.value.copy(showHiddenFiles = intent.show)
            is FileBrowserIntent.DownloadFile -> downloadFile(intent.file)
            is FileBrowserIntent.UploadFile -> uploadFile(intent.localPath)
        }
    }

    private fun loadDirectory(path: String) {
        _uiState.value = _uiState.value.copy(isLoading = true, currentPath = path)
        // TODO: Load directory from FTP server
        _uiState.value = _uiState.value.copy(isLoading = false, isConnected = true)
    }

    private fun navigateUp() {
        val currentPath = _uiState.value.currentPath
        if (currentPath != "/") {
            val parentPath = currentPath.substringBeforeLast("/", "")
            loadDirectory(if (parentPath.isEmpty()) "/" else parentPath)
        }
    }

    private fun toggleSelection(fileName: String) {
        val current = _uiState.value.selectedFiles
        _uiState.value = _uiState.value.copy(
            selectedFiles = if (fileName in current) current - fileName else current + fileName
        )
    }

    private fun downloadFile(file: FileItem) {
        // TODO: Download file
    }

    private fun uploadFile(localPath: String) {
        // TODO: Upload file
    }
}