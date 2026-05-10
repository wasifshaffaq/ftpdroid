package com.ftpdroid.app.data.network.ftp

import org.apache.commons.net.ftp.FTP
import org.apache.commons.net.ftp.FTPClient
import org.apache.commons.net.ftp.FTPFile
import org.apache.commons.net.ftp.FTPReply
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import java.io.InputStream
import java.io.OutputStream

data class FtpFileInfo(
    val name: String,
    val path: String,
    val isDirectory: Boolean,
    val size: Long,
    val lastModified: Long
)

sealed class FtpClientResult<out T> {
    data class Success<T>(val data: T) : FtpClientResult<T>()
    data class Error(val message: String) : FtpClientResult<Nothing>()
}

class FtpClientManager {
    private var ftpClient: FTPClient? = null
    private var progressListener: TransferProgressListener? = null

    val isConnected: Boolean
        get() = ftpClient?.isConnected == true

    suspend fun connect(
        host: String,
        port: Int,
        username: String,
        password: String
    ): FtpClientResult<Boolean> = withContext(Dispatchers.IO) {
        try {
            ftpClient = FTPClient()
            ftpClient?.connect(host, port)
            ftpClient?.login(username, password)
            ftpClient?.enterLocalPassiveMode()
            ftpClient?.setFileType(FTP.BINARY_FILE_TYPE)

            val reply = ftpClient?.replyCode ?: 0
            if (!FTPReply.isPositiveCompletion(reply)) {
                return@withContext FtpClientResult.Error("Connection failed: $reply")
            }
            FtpClientResult.Success(true)
        } catch (e: Exception) {
            FtpClientResult.Error(e.message ?: "Unknown error")
        }
    }

    suspend fun setProgressListener(listener: TransferProgressListener?) {
        progressListener = listener
    }

    private fun parseLong(value: Any?): Long {
        return when (value) {
            is Long -> value
            is Number -> value.toLong()
            is String -> value.toLongOrNull() ?: 0L
            else -> 0L
        }
    }

    suspend fun listDirectory(path: String = "."): FtpClientResult<List<FtpFileInfo>> =
        withContext(Dispatchers.IO) {
            try {
                val result = mutableListOf<FtpFileInfo>()
                val files = ftpClient?.listFiles(path) ?: emptyArray()
                for (ftpFile in files) {
                    if (ftpFile != null) {
                        result.add(FtpFileInfo(
                            name = ftpFile.name,
                            path = if (path.endsWith("/")) "$path${ftpFile.name}" else "$path/${ftpFile.name}",
                            isDirectory = ftpFile.isDirectory,
                            size = ftpFile.size,
                            lastModified = ftpFile.timestamp?.timeInMillis ?: 0L
                        ))
                    }
                }
                FtpClientResult.Success(result)
            } catch (e: Exception) {
                FtpClientResult.Error(e.message ?: "Unknown error")
            }
        }

    suspend fun uploadFile(
        localFile: File,
        remotePath: String
    ): FtpClientResult<Boolean> = withContext(Dispatchers.IO) {
        try {
            val inputStream = FileInputStream(localFile)
            val remoteFileName = remotePath.ifEmpty { localFile.name }
            val fileSize = localFile.length()
            
            // Note: storeFile doesn't give us periodic progress. 
            // We use the stream-based approach to get progress updates.
            val outputStream: OutputStream? = ftpClient?.storeFileStream(remoteFileName)

            if (outputStream == null) {
                inputStream.close()
                return@withContext FtpClientResult.Error("Could not open output stream")
            }

            val buffer = ByteArray(16384)
            var bytesRead: Int
            var totalBytesRead = 0L

            try {
                while (inputStream.read(buffer).also { bytesRead = it } != -1) {
                    outputStream.write(buffer, 0, bytesRead)
                    totalBytesRead += bytesRead
                    progressListener?.onProgress(totalBytesRead, fileSize)
                }
                outputStream.close()
                inputStream.close()
                ftpClient?.completePendingCommand()

                progressListener?.onComplete(true)
                FtpClientResult.Success(true)
            } catch (e: Exception) {
                outputStream.close()
                inputStream.close()
                progressListener?.onError(e.message ?: "Transfer error")
                FtpClientResult.Error(e.message ?: "Transfer error")
            }
        } catch (e: Exception) {
            progressListener?.onError(e.message ?: "Unknown error")
            FtpClientResult.Error(e.message ?: "Unknown error")
        }
    }

    suspend fun uploadFileWithProgress(
        localFile: File,
        remotePath: String
    ): FtpClientResult<Boolean> = uploadFile(localFile, remotePath)

    suspend fun downloadFile(
        remotePath: String,
        localFile: File
    ): FtpClientResult<Boolean> = withContext(Dispatchers.IO) {
        try {
            val outputStream = FileOutputStream(localFile)
            val inputStream: InputStream? = ftpClient?.retrieveFileStream(remotePath)

            if (inputStream == null) {
                outputStream.close()
                return@withContext FtpClientResult.Error("Could not open input stream")
            }

            val buffer = ByteArray(16384)
            var bytesRead: Int
            var totalBytesRead = 0L
            
            // Get file size for progress
            val fileSize = try {
                ftpClient?.listFiles(remotePath)?.firstOrNull()?.size ?: 0L
            } catch (e: Exception) { 0L }

            try {
                while (inputStream.read(buffer).also { bytesRead = it } != -1) {
                    outputStream.write(buffer, 0, bytesRead)
                    totalBytesRead += bytesRead
                    progressListener?.onProgress(totalBytesRead, fileSize)
                }
                outputStream.close()
                inputStream.close()
                ftpClient?.completePendingCommand()

                progressListener?.onComplete(true)
                FtpClientResult.Success(true)
            } catch (e: Exception) {
                outputStream.close()
                inputStream.close()
                progressListener?.onError(e.message ?: "Transfer error")
                FtpClientResult.Error(e.message ?: "Transfer error")
            }
        } catch (e: Exception) {
            progressListener?.onError(e.message ?: "Unknown error")
            FtpClientResult.Error(e.message ?: "Unknown error")
        }
    }

    suspend fun downloadFileWithProgress(
        remotePath: String,
        localFile: File
    ): FtpClientResult<Boolean> = downloadFile(remotePath, localFile)

    suspend fun makeDirectory(path: String): FtpClientResult<Boolean> =
        withContext(Dispatchers.IO) {
            try {
                val success = ftpClient?.makeDirectory(path) == true
                if (success) {
                    FtpClientResult.Success(true)
                } else {
                    FtpClientResult.Error("Could not create directory")
                }
            } catch (e: Exception) {
                FtpClientResult.Error(e.message ?: "Unknown error")
            }
        }

    suspend fun deleteFile(path: String): FtpClientResult<Boolean> =
        withContext(Dispatchers.IO) {
            try {
                val success = ftpClient?.deleteFile(path) == true
                if (success) {
                    FtpClientResult.Success(true)
                } else {
                    FtpClientResult.Error("Could not delete file")
                }
            } catch (e: Exception) {
                FtpClientResult.Error(e.message ?: "Unknown error")
            }
        }

    suspend fun rename(fromPath: String, toPath: String): FtpClientResult<Boolean> =
        withContext(Dispatchers.IO) {
            try {
                val success = ftpClient?.rename(fromPath, toPath) == true
                if (success) {
                    FtpClientResult.Success(true)
                } else {
                    FtpClientResult.Error("Could not rename")
                }
            } catch (e: Exception) {
                FtpClientResult.Error(e.message ?: "Unknown error")
            }
        }

    suspend fun getFileSize(path: String): FtpClientResult<Long> =
        withContext(Dispatchers.IO) {
            try {
                val size = ftpClient?.size(path)?.toLong() ?: -1L
                if (size >= 0) {
                    FtpClientResult.Success(size)
                } else {
                    FtpClientResult.Error("Could not get file size")
                }
            } catch (e: Exception) {
                FtpClientResult.Error(e.message ?: "Unknown error")
            }
        }

    suspend fun removeDirectory(path: String): FtpClientResult<Boolean> =
        withContext(Dispatchers.IO) {
            try {
                val success = ftpClient?.removeDirectory(path) == true
                if (success) {
                    FtpClientResult.Success(true)
                } else {
                    FtpClientResult.Error("Could not remove directory")
                }
            } catch (e: Exception) {
                FtpClientResult.Error(e.message ?: "Unknown error")
            }
        }

    suspend fun changeWorkingDirectory(path: String): FtpClientResult<Boolean> =
        withContext(Dispatchers.IO) {
            try {
                val success = ftpClient?.changeWorkingDirectory(path) == true
                if (success) {
                    FtpClientResult.Success(true)
                } else {
                    FtpClientResult.Error("Could not change directory")
                }
            } catch (e: Exception) {
                FtpClientResult.Error(e.message ?: "Unknown error")
            }
        }

    suspend fun getCurrentDirectory(): FtpClientResult<String> =
        withContext(Dispatchers.IO) {
            try {
                val dir = ftpClient?.printWorkingDirectory()
                if (dir != null) {
                    FtpClientResult.Success(dir)
                } else {
                    FtpClientResult.Error("Could not get current directory")
                }
            } catch (e: Exception) {
                FtpClientResult.Error(e.message ?: "Unknown error")
            }
        }

    fun disconnect() {
        try {
            ftpClient?.logout()
            ftpClient?.disconnect()
        } catch (_: Exception) {
        } finally {
            ftpClient = null
        }
    }
}