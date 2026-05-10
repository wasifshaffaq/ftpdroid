package com.ftpdroid.app.data.repository

import com.ftpdroid.app.data.local.db.dao.ServerUserDao
import com.ftpdroid.app.data.local.db.entity.ServerUserEntity
import com.ftpdroid.app.data.local.prefs.SecureStorage
import com.ftpdroid.app.data.local.prefs.ServerPreferences
import com.ftpdroid.app.data.network.ftp.FtpServerManager
import com.ftpdroid.app.domain.model.ServerConfig
import com.ftpdroid.app.domain.model.ServerUser
import com.ftpdroid.app.domain.repository.ServerRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

import android.content.Context
import android.content.Intent
import com.ftpdroid.app.service.FtpServerService
import dagger.hilt.android.qualifiers.ApplicationContext

@Singleton
class ServerRepositoryImpl @Inject constructor(
    @ApplicationContext private val context: Context,
    private val serverPreferences: ServerPreferences,
    private val serverUserDao: ServerUserDao,
    private val secureStorage: SecureStorage,
    private val ftpServerManager: FtpServerManager
) : ServerRepository {

    override suspend fun startServer(config: ServerConfig): Result<Unit> {
        android.util.Log.d("ServerRepositoryImpl", "startServer() called with config: $config")
        return try {
            val intent = Intent(context, FtpServerService::class.java).apply {
                action = FtpServerService.ACTION_START
            }
            android.util.Log.d("ServerRepositoryImpl", "Sending ACTION_START intent to FtpServerService")
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
                context.startForegroundService(intent)
            } else {
                context.startService(intent)
            }
            serverPreferences.isServerRunning = true
            serverPreferences.serverStartTime = System.currentTimeMillis()
            Result.success(Unit)
        } catch (e: Exception) {
            android.util.Log.e("ServerRepositoryImpl", "Error starting server service", e)
            Result.failure(e)
        }
    }

    override suspend fun stopServer() {
        android.util.Log.d("ServerRepositoryImpl", "stopServer() called")
        val intent = Intent(context, FtpServerService::class.java).apply {
            action = FtpServerService.ACTION_STOP
        }
        android.util.Log.d("ServerRepositoryImpl", "Sending ACTION_STOP intent to FtpServerService")
        context.startService(intent)
        serverPreferences.isServerRunning = false
        serverPreferences.serverStartTime = 0L
    }

    override suspend fun isServerRunning(): Boolean {
        return serverPreferences.isServerRunning
    }

    override suspend fun getServerConfig(): ServerConfig {
        return serverPreferences.getServerConfig()
    }

    override suspend fun saveServerConfig(config: ServerConfig) {
        serverPreferences.saveServerConfig(config)
    }

    override fun getAllUsers(): Flow<List<ServerUser>> {
        return serverUserDao.getAllUsers().map { entities ->
            entities.map { it.toDomain() }
        }
    }

    override suspend fun getUserById(id: Long): ServerUser? {
        return serverUserDao.getUserById(id)?.toDomain()
    }

    override suspend fun getUserByUsername(username: String): ServerUser? {
        return serverUserDao.getUserByUsername(username)?.toDomain()
    }

    override suspend fun addUser(user: ServerUser): Long {
        val entity = ServerUserEntity.fromDomain(user)
        val id = serverUserDao.insertUser(entity)
        // Save password securely
        secureStorage.savePassword(id, user.password)
        return id
    }

    override suspend fun updateUser(user: ServerUser) {
        val entity = ServerUserEntity.fromDomain(user)
        serverUserDao.updateUser(entity)
        // Update password in secure storage
        secureStorage.savePassword(user.id, user.password)
    }

    override suspend fun deleteUser(id: Long) {
        serverUserDao.deleteUserById(id)
        secureStorage.deletePassword(id)
    }

    override suspend fun getUserCount(): Int {
        return serverUserDao.getUserCount()
    }
}