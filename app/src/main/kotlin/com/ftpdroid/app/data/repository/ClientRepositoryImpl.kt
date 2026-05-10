package com.ftpdroid.app.data.repository

import com.ftpdroid.app.domain.model.RemoteEntry
import com.ftpdroid.app.data.network.model.ServerInfo
import com.ftpdroid.app.data.local.db.dao.ProfileDao
import com.ftpdroid.app.data.network.ftp.FtpClientManager
import com.ftpdroid.app.data.network.ftp.SftpClientManager
import com.ftpdroid.app.data.network.ftp.FtpClientResult
import com.ftpdroid.app.domain.model.Protocol
import com.ftpdroid.app.domain.repository.ClientRepository
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ClientRepositoryImpl @Inject constructor(
    private val profileDao: ProfileDao,
    private val ftpClientManager: FtpClientManager,
    private val sftpClientManager: SftpClientManager
) : ClientRepository {

    private var currentProtocol: Protocol? = null

    override suspend fun connect(profileId: Long): Result<ServerInfo> {
        val profile = profileDao.getProfileById(profileId)?.toDomain() 
            ?: return Result.failure(IllegalArgumentException("Profile not found"))
        
        currentProtocol = profile.protocol
        
        val result = when (profile.protocol) {
            Protocol.FTP, Protocol.FTPS_EXPLICIT, Protocol.FTPS_IMPLICIT -> {
                ftpClientManager.connect(profile.host, profile.port, profile.username, profile.password)
            }
            Protocol.SFTP -> {
                sftpClientManager.connect(profile.host, profile.port, profile.username, profile.password)
            }
        }

        return when (result) {
            is FtpClientResult.Success -> {
                Result.success(ServerInfo(
                    host = profile.host,
                    port = profile.port,
                    protocol = profile.protocol.name,
                    isConnected = true,
                    serverType = profile.protocol.name,
                    features = emptyList(),
                    currentDirectory = "/"
                ))
            }
            is FtpClientResult.Error -> Result.failure(Exception(result.message))
            else -> Result.failure(Exception("Unknown error"))
        }
    }

    override suspend fun disconnect() {
        when (currentProtocol) {
            Protocol.FTP, Protocol.FTPS_EXPLICIT, Protocol.FTPS_IMPLICIT -> ftpClientManager.disconnect()
            Protocol.SFTP -> sftpClientManager.disconnect()
            null -> {}
        }
    }

    override suspend fun isConnected(): Boolean {
        return when (currentProtocol) {
            Protocol.FTP, Protocol.FTPS_EXPLICIT, Protocol.FTPS_IMPLICIT -> ftpClientManager.isConnected
            Protocol.SFTP -> sftpClientManager.isConnected
            null -> false
        }
    }

    override suspend fun getCurrentDirectory(): String {
        return when (currentProtocol) {
            Protocol.FTP, Protocol.FTPS_EXPLICIT, Protocol.FTPS_IMPLICIT -> {
                val result = ftpClientManager.getCurrentDirectory()
                if (result is FtpClientResult.Success) result.data else "/"
            }
            Protocol.SFTP -> "/" // SFTP doesn't have a simple PWD in the manager yet
            null -> "/"
        }
    }

    override suspend fun changeDirectory(path: String): Result<Unit> {
        val result = when (currentProtocol) {
            Protocol.FTP, Protocol.FTPS_EXPLICIT, Protocol.FTPS_IMPLICIT -> ftpClientManager.changeWorkingDirectory(path)
            Protocol.SFTP -> FtpClientResult.Success(true) // Placeholder
            null -> FtpClientResult.Error("Not connected")
        }
        return if (result is FtpClientResult.Success) Result.success(Unit) else Result.failure(Exception("Failed"))
    }

    override suspend fun listDirectory(path: String): Result<List<RemoteEntry>> {
        val entries = when (currentProtocol) {
            Protocol.FTP, Protocol.FTPS_EXPLICIT, Protocol.FTPS_IMPLICIT -> {
                val result = ftpClientManager.listDirectory(path)
                if (result is FtpClientResult.Success) {
                    result.data.map { info ->
                        RemoteEntry(
                            name = info.name,
                            path = info.path,
                            isDirectory = info.isDirectory,
                            size = info.size,
                            lastModified = info.lastModified
                        )
                    }
                } else null
            }
            Protocol.SFTP -> {
                val result = sftpClientManager.listDirectory(path)
                if (result is FtpClientResult.Success) {
                    result.data.map { info ->
                        RemoteEntry(
                            name = info.name,
                            path = info.path,
                            isDirectory = info.isDirectory,
                            size = info.size,
                            lastModified = info.lastModified
                        )
                    }
                } else null
            }
            null -> return Result.failure(Exception("Not connected"))
        }

        return if (entries != null) {
            Result.success(entries)
        } else {
            Result.failure(Exception("Failed to list directory"))
        }
    }

    override suspend fun createDirectory(path: String): Result<Unit> {
        val result = when (currentProtocol) {
            Protocol.FTP, Protocol.FTPS_EXPLICIT, Protocol.FTPS_IMPLICIT -> ftpClientManager.makeDirectory(path)
            Protocol.SFTP -> sftpClientManager.makeDirectory(path)
            null -> return Result.failure(Exception("Not connected"))
        }
        return if (result is FtpClientResult.Success) Result.success(Unit) else Result.failure(Exception("Failed"))
    }

    override suspend fun delete(path: String): Result<Unit> {
        val result = when (currentProtocol) {
            Protocol.FTP, Protocol.FTPS_EXPLICIT, Protocol.FTPS_IMPLICIT -> ftpClientManager.deleteFile(path)
            Protocol.SFTP -> sftpClientManager.deleteFile(path)
            null -> return Result.failure(Exception("Not connected"))
        }
        return if (result is FtpClientResult.Success) Result.success(Unit) else Result.failure(Exception("Failed"))
    }

    override suspend fun rename(oldPath: String, newPath: String): Result<Unit> {
        val result = when (currentProtocol) {
            Protocol.FTP, Protocol.FTPS_EXPLICIT, Protocol.FTPS_IMPLICIT -> ftpClientManager.rename(oldPath, newPath)
            Protocol.SFTP -> sftpClientManager.rename(oldPath, newPath)
            null -> return Result.failure(Exception("Not connected"))
        }
        return if (result is FtpClientResult.Success) Result.success(Unit) else Result.failure(Exception("Failed"))
    }

    override suspend fun downloadFile(
        remotePath: String,
        localPath: String,
        onProgress: (Long, Long) -> Unit
    ): Result<Unit> {
        val localFile = java.io.File(localPath)
        val result = when (currentProtocol) {
            Protocol.FTP, Protocol.FTPS_EXPLICIT, Protocol.FTPS_IMPLICIT -> ftpClientManager.downloadFile(remotePath, localFile)
            Protocol.SFTP -> sftpClientManager.downloadFile(remotePath, localFile)
            null -> return Result.failure(Exception("Not connected"))
        }
        return if (result is FtpClientResult.Success) Result.success(Unit) else Result.failure(Exception("Failed"))
    }

    override suspend fun uploadFile(
        localPath: String,
        remotePath: String,
        onProgress: (Long, Long) -> Unit
    ): Result<Unit> {
        val localFile = java.io.File(localPath)
        val result = when (currentProtocol) {
            Protocol.FTP, Protocol.FTPS_EXPLICIT, Protocol.FTPS_IMPLICIT -> ftpClientManager.uploadFile(localFile, remotePath)
            Protocol.SFTP -> sftpClientManager.uploadFile(localFile, remotePath)
            null -> return Result.failure(Exception("Not connected"))
        }
        return if (result is FtpClientResult.Success) Result.success(Unit) else Result.failure(Exception("Failed"))
    }
}