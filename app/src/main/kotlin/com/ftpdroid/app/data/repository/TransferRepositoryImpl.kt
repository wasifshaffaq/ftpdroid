package com.ftpdroid.app.data.repository

import com.ftpdroid.app.data.local.db.dao.TransferDao
import com.ftpdroid.app.data.local.db.entity.TransferEntity
import com.ftpdroid.app.domain.model.Transfer
import com.ftpdroid.app.domain.model.TransferStatus
import com.ftpdroid.app.domain.repository.TransferRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class TransferRepositoryImpl @Inject constructor(
    private val transferDao: TransferDao
) : TransferRepository {

    override fun getAllTransfers(): Flow<List<Transfer>> {
        return transferDao.getAllTransfers().map { entities ->
            entities.map { it.toDomain() }
        }
    }

    override fun getTransfersByProfileId(profileId: Long): Flow<List<Transfer>> {
        return transferDao.getTransfersByProfileId(profileId).map { entities ->
            entities.map { it.toDomain() }
        }
    }

    override fun getTransfersByStatus(status: TransferStatus): Flow<List<Transfer>> {
        return transferDao.getTransfersByStatus(status.name).map { entities ->
            entities.map { it.toDomain() }
        }
    }

    override fun getPendingTransfers(): Flow<List<Transfer>> {
        return transferDao.getPendingTransfers().map { entities ->
            entities.map { it.toDomain() }
        }
    }

    override suspend fun getTransferById(id: Long): Transfer? {
        return transferDao.getTransferById(id)?.toDomain()
    }

    override suspend fun insertTransfer(transfer: Transfer): Long {
        val entity = TransferEntity.fromDomain(transfer)
        return transferDao.insertTransfer(entity)
    }

    override suspend fun updateTransfer(transfer: Transfer) {
        val entity = TransferEntity.fromDomain(transfer)
        transferDao.updateTransfer(entity)
    }

    override suspend fun deleteTransfer(id: Long) {
        transferDao.deleteTransferById(id)
    }

    override suspend fun updateTransferStatus(id: Long, status: TransferStatus) {
        transferDao.updateTransferStatus(id, status.name)
    }

    override suspend fun updateTransferProgress(id: Long, transferredBytes: Long, speed: Long) {
        transferDao.updateTransferProgress(id, transferredBytes, speed)
    }

    override suspend fun clearOldTransfers(daysOld: Int) {
        val timestamp = System.currentTimeMillis() - (daysOld * 24 * 60 * 60 * 1000L)
        transferDao.deleteCompletedTransfersOlderThan(timestamp)
    }
}