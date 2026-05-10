package com.ftpdroid.app.data.local.db.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.ftpdroid.app.data.local.db.entity.ConnectionLogEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface ConnectionLogDao {
    @Query("SELECT * FROM connection_logs ORDER BY timestamp DESC")
    fun getAllLogs(): Flow<List<ConnectionLogEntity>>

    @Query("SELECT * FROM connection_logs WHERE ipAddress = :ipAddress ORDER BY timestamp DESC")
    fun getLogsByIpAddress(ipAddress: String): Flow<List<ConnectionLogEntity>>

    @Query("SELECT * FROM connection_logs WHERE username = :username ORDER BY timestamp DESC")
    fun getLogsByUsername(username: String): Flow<List<ConnectionLogEntity>>

    @Query("SELECT * FROM connection_logs WHERE eventType = :eventType ORDER BY timestamp DESC")
    fun getLogsByEventType(eventType: String): Flow<List<ConnectionLogEntity>>

    @Query("SELECT * FROM connection_logs WHERE id = :id")
    suspend fun getLogById(id: Long): ConnectionLogEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertLog(log: ConnectionLogEntity): Long

    @Delete
    suspend fun deleteLog(log: ConnectionLogEntity)

    @Query("DELETE FROM connection_logs WHERE timestamp < :timestamp")
    suspend fun deleteLogsOlderThan(timestamp: Long)

    @Query("DELETE FROM connection_logs")
    suspend fun clearAllLogs()

    @Query("SELECT COUNT(*) FROM connection_logs")
    suspend fun getLogCount(): Int
}