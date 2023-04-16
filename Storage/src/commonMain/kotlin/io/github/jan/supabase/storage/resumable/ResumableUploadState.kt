package io.github.jan.supabase.storage.resumable

import io.github.jan.supabase.storage.UploadStatus
import io.ktor.utils.io.ByteReadChannel
import kotlin.jvm.JvmInline

/**
 * Represents the state of a resumable upload
 * @param fingerprint The fingerprint of the upload
 * @param status The current upload status
 * @param paused Whether the upload is paused
 */
data class ResumableUploadState(val fingerprint: Fingerprint, val status: UploadStatus, val paused: Boolean) {

    /**
     * Whether the upload is done
     */
    val isDone = status is UploadStatus.Success

    /**
     * The upload progress as a float between 0 and 1
     */
    val progress = if(status is UploadStatus.Progress) {
         status.totalBytesSend.toFloat() / status.contentLength.toFloat()
    } else 1f

}