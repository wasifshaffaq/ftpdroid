package com.ftpdroid.app.ui.component

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Slider
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
fun ConnectionQrDialog(
    serverAddress: String,
    port: Int,
    defaultUser: String,
    defaultPassword: String,
    onDismiss: () -> Unit
) {
    var qrSize by remember { mutableFloatStateOf(200f) }
    var includePassword by remember { mutableStateOf(true) }

    androidx.compose.material3.AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Share Connection") },
        text = {
            Column(modifier = Modifier.fillMaxWidth()) {
                Text(
                    text = "Scan this QR code to quickly connect",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                Spacer(modifier = Modifier.height(16.dp))

                // QR Code placeholder
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(200.dp),
                    colors = androidx.compose.material3.CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surfaceVariant
                    )
                ) {
                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Text(
                            text = "QR Code",
                            style = MaterialTheme.typography.bodyLarge
                        )
                        Text(
                            text = "ftp://$serverAddress:$port",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // QR size slider
                Text(text = "QR Code Size", style = MaterialTheme.typography.labelMedium)
                Slider(
                    value = qrSize,
                    onValueChange = { qrSize = it },
                    valueRange = 100f..300f,
                    steps = 3
                )

                Spacer(modifier = Modifier.height(8.dp))

                // Options
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(text = "Include password", style = MaterialTheme.typography.bodyMedium)
                    Switch(
                        checked = includePassword,
                        onCheckedChange = { includePassword = it }
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))

                // Connection details
                OutlinedTextField(
                    value = defaultUser,
                    onValueChange = {},
                    label = { Text("Username") },
                    readOnly = true,
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(8.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(text = "Host: $serverAddress", style = MaterialTheme.typography.bodySmall)
                    Text(text = "Port: $port", style = MaterialTheme.typography.bodySmall)
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text("Done")
            }
        },
        dismissButton = {
            TextButton(onClick = { /* TODO: Share as text */ }) {
                Text("Share as Text")
            }
        }
    )
}