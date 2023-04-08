package io.github.jan.supabase.storage

import kotlin.jvm.JvmInline

/**
 * Represents the status of a download
 */
sealed interface DownloadStatus {

    /**
     * Represents the progress of a download
     * @param totalBytesReceived The total bytes received
     * @param contentLength The total bytes to receive
     */
    data class DownloadProgress(val totalBytesReceived: Long, val contentLength: Long) : DownloadStatus

    /**
     * Represents the success of a download
     * @param data The downloaded data
     */
    @JvmInline
    value class DownloadSuccess(val data: ByteArray) : DownloadStatus

}

/**
 * Represents the status of an upload
 */
sealed interface UploadStatus {

    /**
     * Represents the progress of an upload
     * @param totalBytesSend The total bytes sent
     * @param contentLength The total bytes to send
     */
    data class UploadProgress(val totalBytesSend: Long, val contentLength: Long) : UploadStatus

    /**
     * Represents the success of an upload
     * @param key The key of the uploaded file
     */
    @JvmInline
    value class UploadSuccess(val key: String) : UploadStatus

}