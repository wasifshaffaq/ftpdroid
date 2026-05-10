package com.ftpdroid.app.data.network.model

data class RemoteFile(
    val name: String,
    val path: String,
    val isDirectory: Boolean,
    val size: Long,
    val lastModified: Long,
    val permissions: String = "",
    val owner: String = "",
    val group: String = "",
    val linkTarget: String? = null
)