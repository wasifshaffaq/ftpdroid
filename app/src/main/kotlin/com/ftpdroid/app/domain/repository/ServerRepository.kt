package com.ftpdroid.app.domain.repository

import com.ftpdroid.app.domain.model.ServerConfig
import com.ftpdroid.app.domain.model.ServerUser
import kotlinx.coroutines.flow.Flow

interface ServerRepository {
    suspend fun startServer(config: ServerConfig): Result<Unit>
    suspend fun stopServer()
    suspend fun isServerRunning(): Boolean
    suspend fun getServerConfig(): ServerConfig
    suspend fun saveServerConfig(config: ServerConfig)

    fun getAllUsers(): Flow<List<ServerUser>>
    suspend fun getUserById(id: Long): ServerUser?
    suspend fun getUserByUsername(username: String): ServerUser?
    suspend fun addUser(user: ServerUser): Long
    suspend fun updateUser(user: ServerUser)
    suspend fun deleteUser(id: Long)
    suspend fun getUserCount(): Int
}