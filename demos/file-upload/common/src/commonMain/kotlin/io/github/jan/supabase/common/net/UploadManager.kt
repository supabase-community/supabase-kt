package io.github.jan.supabase.common.net

import io.github.jan.supabase.storage.resumable.Fingerprint
import io.github.jan.supabase.storage.resumable.ResumableUpload
import java.io.File

sealed interface UploadManager {

    val uploads: Map<Fingerprint, ResumableUpload>

    fun pauseUpload(fingerprint: Fingerprint)

    fun createOrResumeUpload(file: File, path: String): ResumableUpload

    fun cancelUpload(fingerprint: Fingerprint)

}