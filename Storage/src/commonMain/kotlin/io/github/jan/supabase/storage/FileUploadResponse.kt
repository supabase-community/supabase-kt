package io.github.jan.supabase.storage

data class FileUploadResponse(
    val id: String,
    val path: String,
    val key: String
)
