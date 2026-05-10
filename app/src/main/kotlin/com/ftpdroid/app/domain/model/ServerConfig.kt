package com.ftpdroid.app.domain.model

import kotlinx.serialization.Serializable

@Serializable
data class ServerConfig(
    val port: Int = 2121,
    val pasvPortRangeStart: Int = 10000,
    val pasvPortRangeEnd: Int = 10100,
    val maxConnections: Int = 10,
    val idleTimeoutSeconds: Int = 300,
    val enableFtps: Boolean = false,
    val keystorePath: String? = null,
    val keystorePassword: String? = null,
    val enableAnonymous: Boolean = false,
    val anonymousRootPath: String = "",
    val maxUploadBytesPerSec: Long = 0L,
    val maxDownloadBytesPerSec: Long = 0L,
    val allowedIps: List<String> = emptyList(),
    val blockedIps: List<String> = emptyList()
)