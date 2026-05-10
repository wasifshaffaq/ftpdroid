package com.ftpdroid.app.ui.screen.server

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ServerSettingsScreen(
    onNavigateBack: () -> Unit,
    viewModel: ServerSettingsViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(uiState.showSaveSuccess) {
        if (uiState.showSaveSuccess) {
            snackbarHostState.showSnackbar("Settings saved successfully")
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Server Settings") },
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
            Text(
                text = "Connection",
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.primary
            )

            OutlinedTextField(
                value = uiState.port.toString(),
                onValueChange = { it.toIntOrNull()?.let { port -> viewModel.onIntent(ServerSettingsIntent.UpdatePort(port)) } },
                label = { Text("Port") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                modifier = Modifier.fillMaxWidth()
            )

            OutlinedTextField(
                value = uiState.maxConnections.toString(),
                onValueChange = { it.toIntOrNull()?.let { max -> viewModel.onIntent(ServerSettingsIntent.UpdateMaxConnections(max)) } },
                label = { Text("Max Connections") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                modifier = Modifier.fillMaxWidth()
            )

            OutlinedTextField(
                value = uiState.welcomeMessage,
                onValueChange = { viewModel.onIntent(ServerSettingsIntent.UpdateWelcomeMessage(it)) },
                label = { Text("Welcome Message") },
                modifier = Modifier.fillMaxWidth()
            )

            Text(
                text = "FTP Mode",
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier.padding(top = 8.dp)
            )

            SettingsSwitch(
                title = "Passive Mode",
                subtitle = "Enable passive FTP mode",
                checked = uiState.enablePassiveMode,
                onCheckedChange = { viewModel.onIntent(ServerSettingsIntent.TogglePassiveMode(it)) }
            )

            if (uiState.enablePassiveMode) {
                OutlinedTextField(
                    value = uiState.passivePortRange,
                    onValueChange = { viewModel.onIntent(ServerSettingsIntent.UpdatePassivePortRange(it)) },
                    label = { Text("Passive Port Range") },
                    placeholder = { Text("50000-50100") },
                    modifier = Modifier.fillMaxWidth()
                )
            }

            Text(
                text = "Security",
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier.padding(top = 8.dp)
            )

            SettingsSwitch(
                title = "Anonymous Login",
                subtitle = "Allow anonymous FTP access",
                checked = uiState.enableAnonymous,
                onCheckedChange = { viewModel.onIntent(ServerSettingsIntent.ToggleAnonymous(it)) }
            )

            OutlinedTextField(
                value = uiState.allowedIpRanges,
                onValueChange = { viewModel.onIntent(ServerSettingsIntent.UpdateAllowedIpRanges(it)) },
                label = { Text("Allowed IP Ranges") },
                placeholder = { Text("0.0.0.0/0") },
                supportingText = { Text("Comma-separated CIDR ranges") },
                modifier = Modifier.fillMaxWidth()
            )

            SettingsSwitch(
                title = "Enable Logging",
                subtitle = "Log all FTP connections and transfers",
                checked = uiState.enableLogging,
                onCheckedChange = { viewModel.onIntent(ServerSettingsIntent.ToggleLogging(it)) }
            )

            Spacer(modifier = Modifier.height(16.dp))

            androidx.compose.material3.Button(
                onClick = { viewModel.onIntent(ServerSettingsIntent.Save) },
                modifier = Modifier.fillMaxWidth(),
                enabled = !uiState.isLoading
            ) {
                Text(if (uiState.isLoading) "Saving..." else "Save Settings")
            }
        }
    }
}

@Composable
private fun SettingsSwitch(
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