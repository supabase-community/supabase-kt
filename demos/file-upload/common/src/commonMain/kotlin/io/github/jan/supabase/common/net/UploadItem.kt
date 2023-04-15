package io.github.jan.supabase.common.net

import io.github.jan.supabase.storage.UploadStatus

data class UploadItem(
    val fileName: String,
    val path: String,
    val status: UploadStatus,
)
