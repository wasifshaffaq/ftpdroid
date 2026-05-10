package com.ftpdroid.app.data.local.db.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.ftpdroid.app.data.local.db.entity.TransferEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface TransferDao {
    @Query("SELECT * FROM transfers ORDER BY startedAt DESC")
    fun getAllTransfers(): Flow<List<TransferEntity>>

    @Query("SELECT * FROM transfers WHERE profileId = :profileId ORDER BY startedAt DESC")
    fun getTransfersByProfileId(profileId: Long): Flow<List<TransferEntity>>

    @Query("SELECT * FROM transfers WHERE status = :status ORDER BY startedAt DESC")
    fun getTransfersByStatus(status: String): Flow<List<TransferEntity>>

    @Query("SELECT * FROM transfers WHERE id = :id")
    suspend fun getTransferById(id: Long): TransferEntity?

    @Query("SELECT * FROM transfers WHERE status IN ('QUEUED', 'IN_PROGRESS') ORDER BY id ASC")
    fun getPendingTransfers(): Flow<List<TransferEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTransfer(transfer: TransferEntity): Long

    @Update
    suspend fun updateTransfer(transfer: TransferEntity)

    @Delete
    suspend fun deleteTransfer(transfer: TransferEntity)

    @Query("DELETE FROM transfers WHERE id = :id")
    suspend fun deleteTransferById(id: Long)

    @Query("DELETE FROM transfers WHERE completedAt IS NOT NULL AND completedAt < :timestamp")
    suspend fun deleteCompletedTransfersOlderThan(timestamp: Long)

    @Query("UPDATE transfers SET status = :status WHERE id = :id")
    suspend fun updateTransferStatus(id: Long, status: String)

    @Query("UPDATE transfers SET transferredBytes = :transferredBytes, speedBytesPerSec = :speed WHERE id = :id")
    suspend fun updateTransferProgress(id: Long, transferredBytes: Long, speed: Long)
}