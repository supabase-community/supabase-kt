package io.github.jan.supabase.storage

import kotlin.jvm.JvmInline

sealed interface DownloadStatus {

    data class DownloadProgress(val received: Long, val total: Long) : DownloadStatus

    @JvmInline
    value class DownloadSuccess(val data: ByteArray) : DownloadStatus

}

sealed interface UploadStatus {

    data class UploadProgress(val sent: Long, val total: Long) : UploadStatus

    @JvmInline
    value class UploadSuccess(val key: String) : UploadStatus

}