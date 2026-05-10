package com.ftpdroid.app.ui.component

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CloudDownload
import androidx.compose.material.icons.filled.CloudUpload
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material3.Card
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.ftpdroid.app.domain.model.TransferStatus

enum class TransferDirection {
    DOWNLOAD, UPLOAD
}

@Composable
fun TransferProgressCard(
    fileName: String,
    fileSize: Long,
    transferredBytes: Long,
    speed: Long,
    direction: TransferDirection,
    status: TransferStatus,
    onPause: () -> Unit = {},
    onResume: () -> Unit = {},
    onCancel: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    val progress = if (fileSize > 0) {
        (transferredBytes.toFloat() / fileSize).coerceIn(0f, 1f)
    } else 0f
    val animatedProgress by animateFloatAsState(targetValue = progress, label = "progress")

    Card(modifier = modifier.fillMaxWidth()) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = if (direction == TransferDirection.DOWNLOAD) {
                        Icons.Default.CloudDownload
                    } else Icons.Default.CloudUpload,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary
                )
                Spacer(modifier = Modifier.width(12.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = fileName,
                        style = MaterialTheme.typography.titleMedium
                    )
                    Text(
                        text = "${transferredBytes.toReadableSize()} / ${fileSize.toReadableSize()}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            LinearProgressIndicator(
                progress = { animatedProgress },
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(8.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = when (status) {
                        TransferStatus.QUEUED -> "Pending"
                        TransferStatus.IN_PROGRESS -> speed.toReadableSpeed()
                        TransferStatus.PAUSED -> "Paused"
                        TransferStatus.COMPLETED -> "Completed"
                        TransferStatus.FAILED -> "Failed"
                        TransferStatus.CANCELLED -> "Cancelled"
                    },
                    style = MaterialTheme.typography.labelMedium,
                    color = when (status) {
                        TransferStatus.FAILED -> MaterialTheme.colorScheme.error
                        TransferStatus.COMPLETED -> MaterialTheme.colorScheme.primary
                        else -> MaterialTheme.colorScheme.onSurfaceVariant
                    }
                )
                Text(
                    text = "${(progress * 100).toInt()}%",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.primary
                )
            }

            if (status == TransferStatus.IN_PROGRESS && speed > 0) {
                val remainingBytes = fileSize - transferredBytes
                val eta = remainingBytes / speed
                Text(
                    text = "ETA: ${eta.toEtaString()}",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            Row {
                when (status) {
                    TransferStatus.IN_PROGRESS -> {
                        IconButton(onClick = onPause) {
                            Icon(Icons.Default.Pause, contentDescription = "Pause")
                        }
                    }
                    TransferStatus.PAUSED -> {
                        IconButton(onClick = onResume) {
                            Icon(Icons.Default.PlayArrow, contentDescription = "Resume")
                        }
                    }
                    else -> {}
                }
                if (status != TransferStatus.COMPLETED) {
                    IconButton(onClick = onCancel) {
                        Icon(Icons.Default.Stop, contentDescription = "Cancel")
                    }
                }
            }
        }
    }
}
