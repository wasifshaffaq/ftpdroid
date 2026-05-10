package com.ftpdroid.app.domain.model

enum class TransferType { UPLOAD, DOWNLOAD }
enum class TransferStatus { QUEUED, IN_PROGRESS, PAUSED, COMPLETED, FAILED, CANCELLED }

data class Transfer(
    val id: Long = 0,
    val profileId: Long,
    val type: TransferType,
    val localPath: String,
    val remotePath: String,
    val totalBytes: Long,
    val transferredBytes: Long = 0L,
    val status: TransferStatus = TransferStatus.QUEUED,
    val startedAt: Long = System.currentTimeMillis(),
    val completedAt: Long? = null,
    val errorMessage: String? = null,
    val speedBytesPerSec: Long = 0L
)