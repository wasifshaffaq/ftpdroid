package com.ftpdroid.app.domain.repository

import com.ftpdroid.app.domain.model.RemoteEntry
import com.ftpdroid.app.data.network.model.ServerInfo

interface ClientRepository {
    suspend fun connect(profileId: Long): Result<ServerInfo>
    suspend fun disconnect()
    suspend fun isConnected(): Boolean
    suspend fun getCurrentDirectory(): String
    suspend fun changeDirectory(path: String): Result<Unit>
    suspend fun listDirectory(path: String): Result<List<RemoteEntry>>
    suspend fun createDirectory(path: String): Result<Unit>
    suspend fun delete(path: String): Result<Unit>
    suspend fun rename(oldPath: String, newPath: String): Result<Unit>
    suspend fun downloadFile(remotePath: String, localPath: String, onProgress: (Long, Long) -> Unit): Result<Unit>
    suspend fun uploadFile(localPath: String, remotePath: String, onProgress: (Long, Long) -> Unit): Result<Unit>
}