package io.github.jan.supabase.storage

data class FileUploadResponse(
    val id: String? = null,
    val path: String,
    val key: String? = null
)
