package io.github.jan.supabase.storage

data class StorageItem(
    val path: String,
    val bucketId: String,
    val authenticated: Boolean
)
