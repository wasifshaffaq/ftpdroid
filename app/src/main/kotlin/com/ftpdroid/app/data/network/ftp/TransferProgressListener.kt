package com.ftpdroid.app.data.network.ftp

interface TransferProgressListener {
    fun onProgress(bytesTransferred: Long, totalBytes: Long)
    fun onComplete(success: Boolean)
    fun onError(message: String)
}

class SimpleTransferProgressListener : TransferProgressListener {
    private var lastReportedProgress = 0L

    override fun onProgress(bytesTransferred: Long, totalBytes: Long) {
        lastReportedProgress = bytesTransferred
    }

    override fun onComplete(success: Boolean) {
        lastReportedProgress = 0L
    }

    override fun onError(message: String) {
        lastReportedProgress = 0L
    }
}
