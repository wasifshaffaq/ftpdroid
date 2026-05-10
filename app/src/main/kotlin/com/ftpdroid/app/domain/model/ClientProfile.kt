package com.ftpdroid.app.domain.model

enum class Protocol { FTP, FTPS_EXPLICIT, FTPS_IMPLICIT, SFTP }

data class ClientProfile(
    val id: Long = 0,
    val name: String,
    val host: String,
    val port: Int = 21,
    val protocol: Protocol = Protocol.FTP,
    val username: String,
    val password: String = "",
    val initialRemotePath: String = "/",
    val isAnonymous: Boolean = false,
    val privateKeyPath: String? = null,
    val passiveMode: Boolean = true,
    val characterEncoding: String = "UTF-8",
    val timeout: Int = 30,
    val createdAt: Long = System.currentTimeMillis()
)