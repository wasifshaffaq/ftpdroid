package com.ftpdroid.app.domain.repository

import com.ftpdroid.app.domain.model.Transfer
import com.ftpdroid.app.domain.model.TransferStatus
import kotlinx.coroutines.flow.Flow

interface TransferRepository {
    fun getAllTransfers(): Flow<List<Transfer>>
    fun getTransfersByProfileId(profileId: Long): Flow<List<Transfer>>
    fun getTransfersByStatus(status: TransferStatus): Flow<List<Transfer>>
    fun getPendingTransfers(): Flow<List<Transfer>>
    suspend fun getTransferById(id: Long): Transfer?
    suspend fun insertTransfer(transfer: Transfer): Long
    suspend fun updateTransfer(transfer: Transfer)
    suspend fun deleteTransfer(id: Long)
    suspend fun updateTransferStatus(id: Long, status: TransferStatus)
    suspend fun updateTransferProgress(id: Long, transferredBytes: Long, speed: Long)
    suspend fun clearOldTransfers(daysOld: Int)
}