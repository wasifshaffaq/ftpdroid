package com.ftpdroid.app.ui.component

import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.os.Build
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.InsertDriveFile
import androidx.compose.material.icons.filled.AudioFile
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.VideoFile
import androidx.compose.ui.graphics.vector.ImageVector
import java.text.DecimalFormat
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.concurrent.TimeUnit

// Context Extensions
fun Context.isNetworkAvailable(): Boolean {
    val connectivityManager = getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
    val network = connectivityManager.activeNetwork ?: return false
    val capabilities = connectivityManager.getNetworkCapabilities(network) ?: return false
    return capabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
}

// Long Extensions
fun Long.toFormattedDate(pattern: String = "yyyy-MM-dd HH:mm:ss"): String {
    return try {
        val sdf = SimpleDateFormat(pattern, Locale.getDefault())
        sdf.format(Date(this))
    } catch (e: Exception) {
        ""
    }
}

fun Long.toHumanReadableSize(): String {
    if (this <= 0) return "0 B"
    val units = arrayOf("B", "KB", "MB", "GB", "TB")
    val digitGroups = (Math.log10(toDouble()) / Math.log10(1024.0)).toInt()
    val df = DecimalFormat("#,##0.##")
    return "${df.format(this / Math.pow(1024.0, digitGroups.toDouble()))} ${units[digitGroups]}"
}

fun Long.toHumanReadableSpeed(): String {
    return "${this.toHumanReadableSize()}/s"
}

// String Extensions
fun String.normalizePath(): String {
    var path = this.replace("\\", "/")
    while (path.contains("//")) {
        path = path.replace("//", "/")
    }
    if (path.length > 1 && path.endsWith("/")) {
        path = path.dropLast(1)
    }
    return path
}

fun String.ensureTrailingSlash(): String {
    return if (endsWith("/")) this else "$this/"
}

fun String.removeTrailingSlash(): String {
    return if (endsWith("/") && length > 1) dropLast(1) else this
}

// Int Extensions
fun Int.toPortString(): String = this.toString()

// Boolean Extensions
fun Boolean.toYesNo(): String = if (this) "Yes" else "No"

// ByteArray Extensions
fun ByteArray.toHexString(): String {
    return joinToString("") { "%02x".format(it) }
}

// List Extensions
fun <T> List<T>.takeLast(n: Int): List<T> {
    return if (size <= n) this else subList(size - n, size)
}

// Duration formatting
fun Long.formatDuration(): String {
    val seconds = this / 1000
    val minutes = seconds / 60
    val hours = minutes / 60

    return when {
        hours > 0 -> "${hours}h ${minutes % 60}m"
        minutes > 0 -> "${minutes}m ${seconds % 60}s"
        else -> "${seconds}s"
    }
}

// File extension helpers
fun String.getFileExtension(): String {
    val index = lastIndexOf('.')
    return if (index > 0) substring(index + 1).lowercase() else ""
}

fun String.isImageFile(): Boolean {
    val ext = getFileExtension()
    return ext in listOf("jpg", "jpeg", "png", "gif", "bmp", "webp")
}

fun String.isAudioFile(): Boolean {
    val ext = getFileExtension()
    return ext in listOf("mp3", "wav", "flac", "aac", "ogg", "m4a")
}

fun String.isVideoFile(): Boolean {
    val ext = getFileExtension()
    return ext in listOf("mp4", "mkv", "avi", "mov", "wmv", "flv", "webm")
}

// Transfer utility extensions
fun Long.toReadableSize(): String = toHumanReadableSize()

fun Long.toReadableSpeed(): String = toHumanReadableSpeed()

fun Long.toEtaString(): String {
    if (this <= 0) return "Calculating..."
    val seconds = this
    val minutes = seconds / 60
    val hours = minutes / 60
    val days = hours / 24

    return when {
        days > 0 -> "${days}d ${hours % 24}h"
        hours > 0 -> "${hours}h ${minutes % 60}m"
        minutes > 0 -> "${minutes}m ${seconds % 60}s"
        else -> "${seconds}s"
    }
}

fun Long.toRelativeDate(): String {
    if (this <= 0) return ""
    val now = System.currentTimeMillis()
    val diff = now - this

    val seconds = TimeUnit.MILLISECONDS.toSeconds(diff)
    val minutes = TimeUnit.MILLISECONDS.toMinutes(diff)
    val hours = TimeUnit.MILLISECONDS.toHours(diff)
    val days = TimeUnit.MILLISECONDS.toDays(diff)

    val df = SimpleDateFormat("MMM dd, HH:mm", Locale.getDefault())
    return when {
        seconds < 60 -> "Just now"
        minutes < 60 -> "${minutes}m ago"
        hours < 24 -> "${hours}h ago"
        days < 7 -> "${days}d ago"
        else -> df.format(Date(this))
    }
}

fun String.toFileIcon(): ImageVector {
    val extension = getFileExtension()
    return when (extension) {
        "jpg", "jpeg", "png", "gif", "bmp", "webp", "svg", "ico" -> Icons.Default.Image
        "mp3", "wav", "flac", "aac", "ogg", "wma", "m4a" -> Icons.Default.AudioFile
        "mp4", "avi", "mkv", "mov", "wmv", "flv", "webm" -> Icons.Default.VideoFile
        "pdf", "doc", "docx", "txt", "rtf", "odt", "xls", "xlsx", "ppt", "pptx" -> Icons.Default.Description
        "json", "xml", "yaml", "yml", "ini", "conf", "cfg", "properties" -> Icons.Default.Settings
        else -> Icons.AutoMirrored.Filled.InsertDriveFile
    }
}

fun calculateSpeed(bytesTransferred: Long, elapsedMillis: Long): Long {
    if (elapsedMillis <= 0) return 0
    return (bytesTransferred * 1000) / elapsedMillis
}

fun calculateProgress(transferred: Long, total: Long): Float {
    if (total <= 0L) return 0f
    return (transferred.toFloat() / total.toFloat()).coerceIn(0f, 1f)
}