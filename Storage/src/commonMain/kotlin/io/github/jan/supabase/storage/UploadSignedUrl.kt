package io.github.jan.supabase.storage

data class UploadSignedUrl(
    val url: String,
    val path: String,
    val token: String
)
