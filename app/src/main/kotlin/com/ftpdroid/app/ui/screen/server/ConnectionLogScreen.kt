package com.ftpdroid.app.ui.screen.server

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
import androidx.compose.material.icons.automirrored.filled.ExitToApp
import androidx.compose.material.icons.automirrored.filled.List
import androidx.compose.material.icons.automirrored.filled.Login
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.DeleteSweep
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.Error
import androidx.compose.material.icons.filled.Upload
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
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.ftpdroid.app.ui.component.toRelativeDate

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ConnectionLogScreen(
    onNavigateBack: () -> Unit,
    viewModel: ConnectionLogViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Connection Log") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    IconButton(onClick = { viewModel.onIntent(ConnectionLogIntent.ClearLogs) }) {
                        Icon(Icons.Default.DeleteSweep, contentDescription = "Clear Logs")
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
                    selected = uiState.filterType == LogFilterType.ALL,
                    onClick = { viewModel.onIntent(ConnectionLogIntent.SetFilter(LogFilterType.ALL)) },
                    label = { Text("All") }
                )
                FilterChip(
                    selected = uiState.filterType == LogFilterType.CONNECTIONS,
                    onClick = { viewModel.onIntent(ConnectionLogIntent.SetFilter(LogFilterType.CONNECTIONS)) },
                    label = { Text("Connections") }
                )
                FilterChip(
                    selected = uiState.filterType == LogFilterType.TRANSFERS,
                    onClick = { viewModel.onIntent(ConnectionLogIntent.SetFilter(LogFilterType.TRANSFERS)) },
                    label = { Text("Transfers") }
                )
                FilterChip(
                    selected = uiState.filterType == LogFilterType.ERRORS,
                    onClick = { viewModel.onIntent(ConnectionLogIntent.SetFilter(LogFilterType.ERRORS)) },
                    label = { Text("Errors") }
                )
            }

            Box(modifier = Modifier.fillMaxSize()) {
                if (uiState.isLoading) {
                    CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
                } else if (uiState.logs.isEmpty()) {
                    Text(
                        text = "No logs available",
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.align(Alignment.Center)
                    )
                } else {
                    LazyColumn(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        items(uiState.logs) { log ->
                            LogEntryCard(log = log)
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun LogEntryCard(log: ConnectionLogEntry) {
    val (icon, color) = when (log.action) {
        LogAction.CONNECTED -> Icons.AutoMirrored.Filled.Login to MaterialTheme.colorScheme.primary
        LogAction.DISCONNECTED -> Icons.AutoMirrored.Filled.ExitToApp to MaterialTheme.colorScheme.onSurfaceVariant
        LogAction.AUTH_FAILED -> Icons.Default.Error to MaterialTheme.colorScheme.error
        LogAction.UPLOAD -> Icons.Default.Upload to MaterialTheme.colorScheme.tertiary
        LogAction.DOWNLOAD -> Icons.Default.Download to MaterialTheme.colorScheme.secondary
        LogAction.LIST -> Icons.AutoMirrored.Filled.List to MaterialTheme.colorScheme.onSurfaceVariant
        LogAction.DELETE -> Icons.Default.Delete to MaterialTheme.colorScheme.error
    }

    Card(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = color
            )
            Spacer(modifier = Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = log.action.name.replace("_", " "),
                    style = MaterialTheme.typography.titleSmall
                )
                Text(
                    text = "${log.ipAddress}${log.username?.let { " ($it)" } ?: ""}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                if (log.details.isNotEmpty()) {
                    Text(
                        text = log.details,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
            Text(
                text = log.timestamp.toRelativeDate(),
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}