package com.ftpdroid.app.domain.model

data class ServerUser(
    val id: Long = 0,
    val username: String,
    val password: String,
    val homeDirectory: String,
    val canRead: Boolean = true,
    val canWrite: Boolean = true,
    val canDelete: Boolean = false,
    val maxLoginFailures: Int = 3
)