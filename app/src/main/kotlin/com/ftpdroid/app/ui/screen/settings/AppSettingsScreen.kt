package com.ftpdroid.app.ui.screen.settings

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Palette
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material.icons.filled.Storage
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.ftpdroid.app.data.local.prefs.AppTheme

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AppSettingsScreen(
    onNavigateBack: () -> Unit,
    viewModel: AppSettingsViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }
    var showThemeDialog by remember { mutableStateOf(false) }

    LaunchedEffect(uiState.showSaveSuccess) {
        if (uiState.showSaveSuccess) {
            snackbarHostState.showSnackbar("Settings saved successfully")
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Settings") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Appearance Section
            SettingsSectionHeader(title = "Appearance", icon = Icons.Default.Palette)

            SettingsClickableItem(
                title = "Theme",
                subtitle = uiState.theme.displayName,
                onClick = { showThemeDialog = true }
            )

            // Notifications Section
            SettingsSectionHeader(title = "Notifications", icon = Icons.Default.Notifications)

            SettingsSwitchItem(
                title = "Push Notifications",
                subtitle = "Show notifications for transfer completion",
                checked = uiState.notificationsEnabled,
                onCheckedChange = { viewModel.onIntent(AppSettingsIntent.ToggleNotifications(it)) }
            )

            // Server Section
            SettingsSectionHeader(title = "Server", icon = Icons.Default.Storage)

            SettingsSwitchItem(
                title = "Auto-start Server",
                subtitle = "Start FTP server on app launch",
                checked = uiState.autoStartServer,
                onCheckedChange = { viewModel.onIntent(AppSettingsIntent.ToggleAutoStartServer(it)) }
            )

            SettingsSwitchItem(
                title = "Keep Screen On",
                subtitle = "Prevent screen from turning off while server is running",
                checked = uiState.keepScreenOn,
                onCheckedChange = { viewModel.onIntent(AppSettingsIntent.ToggleKeepScreenOn(it)) }
            )

            // Transfer Section
            SettingsSectionHeader(title = "Transfer", icon = Icons.Default.Speed)

            OutlinedTextField(
                value = uiState.downloadPath,
                onValueChange = { viewModel.onIntent(AppSettingsIntent.UpdateDownloadPath(it)) },
                label = { Text("Download Path") },
                modifier = Modifier.fillMaxWidth(),
                trailingIcon = {
                    Icon(Icons.Default.Folder, contentDescription = "Browse")
                }
            )

            OutlinedTextField(
                value = uiState.maxConcurrentTransfers.toString(),
                onValueChange = {
                    it.toIntOrNull()?.let { count ->
                        viewModel.onIntent(AppSettingsIntent.UpdateMaxConcurrentTransfers(count))
                    }
                },
                label = { Text("Max Concurrent Transfers") },
                modifier = Modifier.fillMaxWidth()
            )

            OutlinedTextField(
                value = uiState.bufferSize.toString(),
                onValueChange = {
                    it.toIntOrNull()?.let { size ->
                        viewModel.onIntent(AppSettingsIntent.UpdateBufferSize(size))
                    }
                },
                label = { Text("Buffer Size (bytes)") },
                supportingText = { Text("Larger buffer = faster transfers but more RAM") },
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(16.dp))

            androidx.compose.material3.Button(
                onClick = { viewModel.onIntent(AppSettingsIntent.Save) },
                modifier = Modifier.fillMaxWidth(),
                enabled = !uiState.isLoading
            ) {
                Text(if (uiState.isLoading) "Saving..." else "Save Settings")
            }
        }
    }

    if (showThemeDialog) {
        AlertDialog(
            onDismissRequest = { showThemeDialog = false },
            title = { Text("Select Theme") },
            text = {
                Column {
                    AppTheme.entries.forEach { theme ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    viewModel.onIntent(AppSettingsIntent.UpdateTheme(theme))
                                    showThemeDialog = false
                                }
                                .padding(vertical = 8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            RadioButton(
                                selected = uiState.theme == theme,
                                onClick = {
                                    viewModel.onIntent(AppSettingsIntent.UpdateTheme(theme))
                                    showThemeDialog = false
                                }
                            )
                            Text(
                                text = theme.displayName,
                                modifier = Modifier.padding(start = 8.dp)
                            )
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { showThemeDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }
}

@Composable
private fun SettingsSectionHeader(title: String, icon: androidx.compose.ui.graphics.vector.ImageVector) {
    Row(
        modifier = Modifier.padding(vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.primary
        )
        Text(
            text = title,
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.primary,
            modifier = Modifier.padding(start = 8.dp)
        )
    }
}

@Composable
private fun SettingsClickableItem(
    title: String,
    subtitle: String,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(vertical = 8.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(text = title, style = MaterialTheme.typography.bodyLarge)
            Text(
                text = subtitle,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun SettingsSwitchItem(
    title: String,
    subtitle: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(text = title, style = MaterialTheme.typography.bodyLarge)
            Text(
                text = subtitle,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        Switch(checked = checked, onCheckedChange = onCheckedChange)
    }
}