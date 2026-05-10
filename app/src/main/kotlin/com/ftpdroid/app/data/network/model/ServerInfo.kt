package com.ftpdroid.app.data.network.model

data class ServerInfo(
    val host: String,
    val port: Int,
    val protocol: String,
    val isConnected: Boolean,
    val serverType: String = "",
    val serverVersion: String = "",
    val systemType: String = "",
    val features: List<String> = emptyList(),
    val currentDirectory: String = "",
    val lastResponseTime: Long = 0L
)