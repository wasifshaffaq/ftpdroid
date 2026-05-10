package com.ftpdroid.app.data.local.db.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.ftpdroid.app.domain.model.ConnectionLog
import com.ftpdroid.app.domain.model.LogEventType

@Entity(tableName = "connection_logs")
data class ConnectionLogEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val ipAddress: String,
    val username: String,
    val eventType: String,
    val path: String,
    val message: String = "",
    val timestamp: Long = System.currentTimeMillis()
) {
    fun toDomain(): ConnectionLog = ConnectionLog(
        id = id,
        ipAddress = ipAddress,
        username = username,
        eventType = LogEventType.valueOf(eventType),
        path = path,
        message = message,
        timestamp = timestamp
    )

    companion object {
        fun fromDomain(log: ConnectionLog): ConnectionLogEntity = ConnectionLogEntity(
            id = log.id,
            ipAddress = log.ipAddress,
            username = log.username,
            eventType = log.eventType.name,
            path = log.path,
            message = log.message,
            timestamp = log.timestamp
        )
    }
}