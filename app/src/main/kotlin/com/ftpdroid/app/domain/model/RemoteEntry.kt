package com.ftpdroid.app.domain.model

data class RemoteEntry(
    val name: String,
    val path: String,
    val isDirectory: Boolean,
    val size: Long,
    val lastModified: Long,
    val permissions: String = "",
    val owner: String = "",
    val group: String = ""
)