package com.ftpdroid.app.data.local.db.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.ftpdroid.app.domain.model.Transfer
import com.ftpdroid.app.domain.model.TransferStatus
import com.ftpdroid.app.domain.model.TransferType

@Entity(tableName = "transfers")
data class TransferEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val profileId: Long,
    val type: String,
    val localPath: String,
    val remotePath: String,
    val totalBytes: Long,
    val transferredBytes: Long = 0L,
    val status: String = "QUEUED",
    val startedAt: Long = System.currentTimeMillis(),
    val completedAt: Long? = null,
    val errorMessage: String? = null,
    val speedBytesPerSec: Long = 0L
) {
    fun toDomain(): Transfer = Transfer(
        id = id,
        profileId = profileId,
        type = TransferType.valueOf(type),
        localPath = localPath,
        remotePath = remotePath,
        totalBytes = totalBytes,
        transferredBytes = transferredBytes,
        status = TransferStatus.valueOf(status),
        startedAt = startedAt,
        completedAt = completedAt,
        errorMessage = errorMessage,
        speedBytesPerSec = speedBytesPerSec
    )

    companion object {
        fun fromDomain(transfer: Transfer): TransferEntity = TransferEntity(
            id = transfer.id,
            profileId = transfer.profileId,
            type = transfer.type.name,
            localPath = transfer.localPath,
            remotePath = transfer.remotePath,
            totalBytes = transfer.totalBytes,
            transferredBytes = transfer.transferredBytes,
            status = transfer.status.name,
            startedAt = transfer.startedAt,
            completedAt = transfer.completedAt,
            errorMessage = transfer.errorMessage,
            speedBytesPerSec = transfer.speedBytesPerSec
        )
    }
}