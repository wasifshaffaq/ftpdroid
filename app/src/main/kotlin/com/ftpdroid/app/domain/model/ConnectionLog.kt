package com.ftpdroid.app.domain.model

enum class LogEventType { CONNECT, DISCONNECT, DOWNLOAD, UPLOAD, DELETE, ERROR }

data class ConnectionLog(
    val id: Long = 0,
    val ipAddress: String,
    val username: String,
    val eventType: LogEventType,
    val path: String,
    val message: String = "",
    val timestamp: Long = System.currentTimeMillis()
)