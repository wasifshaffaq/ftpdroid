package com.ftpdroid.app.data.network.ftp

import net.schmizz.sshj.SSHClient
import net.schmizz.sshj.sftp.SFTPClient
import net.schmizz.sshj.transport.verification.HostKeyVerifier
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File

data class SftpFileInfo(
    val name: String,
    val path: String,
    val isDirectory: Boolean,
    val size: Long,
    val lastModified: Long,
    val permissions: String
)

class SftpClientManager {
    private var sshClient: SSHClient? = null
    private var sftpClient: SFTPClient? = null
    private var progressListener: TransferProgressListener? = null

    val isConnected: Boolean
        get() = sshClient?.isConnected == true

    private val trustAllVerifier = object : HostKeyVerifier {
        override fun verify(hostname: String?, port: Int, key: java.security.PublicKey?): Boolean = true
        override fun findExistingAlgorithms(
            p0: String?,
            p1: Int
        ): List<String?>? {
            TODO("Not yet implemented")
        }
    }

    suspend fun connect(
        host: String,
        port: Int,
        username: String,
        password: String
    ): FtpClientResult<Boolean> = withContext(Dispatchers.IO) {
        try {
            sshClient = SSHClient()
            sshClient!!.addHostKeyVerifier(trustAllVerifier)
            sshClient!!.connect(host, port)
            sshClient!!.authPassword(username, password)
            sftpClient = sshClient!!.newSFTPClient()

            if (sftpClient != null) {
                FtpClientResult.Success(true)
            } else {
                FtpClientResult.Error("Could not create SFTP client")
            }
        } catch (e: Exception) {
            FtpClientResult.Error(e.message ?: "Unknown error")
        }
    }

    suspend fun connectWithKey(
        host: String,
        port: Int,
        username: String,
        keyFile: File,
        keyPassphrase: String? = null
    ): FtpClientResult<Boolean> = withContext(Dispatchers.IO) {
        try {
            sshClient = SSHClient()
            sshClient!!.addHostKeyVerifier(trustAllVerifier)
            sshClient!!.connect(host, port)

            val keyProvider = sshClient!!.loadKeys(keyFile.absolutePath, keyPassphrase)
            sshClient!!.authPublickey(username, keyProvider)

            sftpClient = sshClient!!.newSFTPClient()

            if (sftpClient != null) {
                FtpClientResult.Success(true)
            } else {
                FtpClientResult.Error("Could not create SFTP client")
            }
        } catch (e: Exception) {
            FtpClientResult.Error(e.message ?: "Unknown error")
        }
    }

    suspend fun setProgressListener(listener: TransferProgressListener?) {
        progressListener = listener
    }

    suspend fun listDirectory(path: String): FtpClientResult<List<SftpFileInfo>> =
        withContext(Dispatchers.IO) {
            try {
                val client = sftpClient ?: return@withContext FtpClientResult.Error("Not connected")
                val files = client.ls(path).map { lsEntry ->
                    SftpFileInfo(
                        name = lsEntry.name,
                        path = if (path.endsWith("/")) "$path${lsEntry.name}" else "$path/${lsEntry.name}",
                        isDirectory = lsEntry.isDirectory,
                        size = lsEntry.attributes?.size ?: 0L,
                        lastModified = (lsEntry.attributes?.atime ?: 0L) * 1000,
                        permissions = ""
                    )
                }
                FtpClientResult.Success(files)
            } catch (e: Exception) {
                FtpClientResult.Error(e.message ?: "Unknown error")
            }
        }

    suspend fun uploadFile(
        localFile: File,
        remotePath: String
    ): FtpClientResult<Boolean> = withContext(Dispatchers.IO) {
        try {
            val client = sftpClient ?: return@withContext FtpClientResult.Error("Not connected")
            client.put(localFile.absolutePath, remotePath)
            progressListener?.onComplete(true)
            FtpClientResult.Success(true)
        } catch (e: Exception) {
            progressListener?.onError(e.message ?: "Unknown error")
            FtpClientResult.Error(e.message ?: "Unknown error")
        }
    }

    suspend fun downloadFile(
        remotePath: String,
        localFile: File
    ): FtpClientResult<Boolean> = withContext(Dispatchers.IO) {
        try {
            val client = sftpClient ?: return@withContext FtpClientResult.Error("Not connected")
            client.get(remotePath, localFile.absolutePath)
            progressListener?.onComplete(true)
            FtpClientResult.Success(true)
        } catch (e: Exception) {
            progressListener?.onError(e.message ?: "Unknown error")
            FtpClientResult.Error(e.message ?: "Unknown error")
        }
    }

    suspend fun makeDirectory(path: String): FtpClientResult<Boolean> =
        withContext(Dispatchers.IO) {
            try {
                val client = sftpClient ?: return@withContext FtpClientResult.Error("Not connected")
                client.mkdir(path)
                FtpClientResult.Success(true)
            } catch (e: Exception) {
                FtpClientResult.Error(e.message ?: "Unknown error")
            }
        }

    suspend fun deleteFile(path: String): FtpClientResult<Boolean> =
        withContext(Dispatchers.IO) {
            try {
                val client = sftpClient ?: return@withContext FtpClientResult.Error("Not connected")
                client.rm(path)
                FtpClientResult.Success(true)
            } catch (e: Exception) {
                FtpClientResult.Error(e.message ?: "Unknown error")
            }
        }

    suspend fun rename(fromPath: String, toPath: String): FtpClientResult<Boolean> =
        withContext(Dispatchers.IO) {
            try {
                val client = sftpClient ?: return@withContext FtpClientResult.Error("Not connected")
                client.rename(fromPath, toPath)
                FtpClientResult.Success(true)
            } catch (e: Exception) {
                FtpClientResult.Error(e.message ?: "Unknown error")
            }
        }

    suspend fun getFileSize(path: String): FtpClientResult<Long> =
        withContext(Dispatchers.IO) {
            try {
                val client = sftpClient ?: return@withContext FtpClientResult.Error("Not connected")
                val size = client.stat(path).size
                FtpClientResult.Success(size)
            } catch (e: Exception) {
                FtpClientResult.Error(e.message ?: "Unknown error")
            }
        }

    suspend fun removeDirectory(path: String): FtpClientResult<Boolean> =
        withContext(Dispatchers.IO) {
            try {
                val client = sftpClient ?: return@withContext FtpClientResult.Error("Not connected")
                client.rmdir(path)
                FtpClientResult.Success(true)
            } catch (e: Exception) {
                FtpClientResult.Error(e.message ?: "Unknown error")
            }
        }

    fun findExistingAlgorithms(): List<String> {
        return emptyList()
    }

    fun disconnect() {
        try {
            sftpClient?.close()
            sshClient?.disconnect()
        } catch (_: Exception) {
        } finally {
            sftpClient = null
            sshClient = null
        }
    }
}