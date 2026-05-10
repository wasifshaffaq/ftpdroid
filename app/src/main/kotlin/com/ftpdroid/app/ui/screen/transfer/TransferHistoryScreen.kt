package com.ftpdroid.app.ui.screen.transfer

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.CloudDownload
import androidx.compose.material.icons.filled.CloudUpload
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.DeleteSweep
import androidx.compose.material.icons.filled.Error
import androidx.compose.material.icons.filled.History
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.ftpdroid.app.ui.component.toReadableSize
import com.ftpdroid.app.ui.component.toRelativeDate

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TransferHistoryScreen(
    onNavigateBack: () -> Unit,
    viewModel: TransferHistoryViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Transfer History") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    if (uiState.transfers.isNotEmpty()) {
                        IconButton(onClick = { viewModel.onIntent(TransferHistoryIntent.ClearHistory) }) {
                            Icon(Icons.Default.DeleteSweep, contentDescription = "Clear History")
                        }
                    }
                }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            // Filter chips
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                FilterChip(
                    selected = uiState.filterStatus == HistoryFilter.ALL,
                    onClick = { viewModel.onIntent(TransferHistoryIntent.SetFilter(HistoryFilter.ALL)) },
                    label = { Text("All") }
                )
                FilterChip(
                    selected = uiState.filterStatus == HistoryFilter.COMPLETED,
                    onClick = { viewModel.onIntent(TransferHistoryIntent.SetFilter(HistoryFilter.COMPLETED)) },
                    label = { Text("Completed") }
                )
                FilterChip(
                    selected = uiState.filterStatus == HistoryFilter.FAILED,
                    onClick = { viewModel.onIntent(TransferHistoryIntent.SetFilter(HistoryFilter.FAILED)) },
                    label = { Text("Failed") }
                )
            }

            Box(modifier = Modifier.fillMaxSize()) {
                if (uiState.isLoading) {
                    CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
                } else if (uiState.transfers.isEmpty()) {
                    Column(
                        modifier = Modifier.align(Alignment.Center),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Icon(
                            imageVector = Icons.Default.History,
                            contentDescription = null,
                            modifier = Modifier.padding(bottom = 16.dp),
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            text = "No transfer history",
                            style = MaterialTheme.typography.bodyLarge,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                } else {
                    val filteredTransfers = when (uiState.filterStatus) {
                        HistoryFilter.ALL -> uiState.transfers
                        HistoryFilter.COMPLETED -> uiState.transfers.filter { it.status == TransferStatus.COMPLETED }
                        HistoryFilter.FAILED -> uiState.transfers.filter { it.status == TransferStatus.FAILED }
                    }

                    LazyColumn(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        items(filteredTransfers) { transfer ->
                            HistoryCard(
                                transfer = transfer,
                                onDelete = { viewModel.onIntent(TransferHistoryIntent.DeleteHistoryItem(transfer.id)) }
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun HistoryCard(
    transfer: TransferHistoryItem,
    onDelete: () -> Unit
) {
    val (icon, color) = when (transfer.status) {
        TransferStatus.COMPLETED -> Icons.Default.CheckCircle to MaterialTheme.colorScheme.primary
        TransferStatus.FAILED -> Icons.Default.Error to MaterialTheme.colorScheme.error
        else -> Icons.Default.CloudDownload to MaterialTheme.colorScheme.onSurfaceVariant
    }

    Card(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = if (transfer.direction == TransferDirection.DOWNLOAD) {
                    Icons.Default.CloudDownload
                } else Icons.Default.CloudUpload,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary
            )
            Spacer(modifier = Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = transfer.fileName,
                    style = MaterialTheme.typography.titleSmall,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = transfer.profileName,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Row {
                    Text(
                        text = transfer.fileSize.toReadableSize(),
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = transfer.endTime.toRelativeDate(),
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                transfer.errorMessage?.let {
                    Text(
                        text = it,
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.error
                    )
                }
            }
            Icon(
                imageVector = icon,
                contentDescription = transfer.status.name,
                tint = color
            )
            IconButton(onClick = onDelete) {
                Icon(Icons.Default.Delete, contentDescription = "Delete")
            }
        }
    }
}