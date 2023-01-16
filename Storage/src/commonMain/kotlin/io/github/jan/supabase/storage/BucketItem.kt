package io.github.jan.supabase.storage

import kotlinx.datetime.Instant
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonObject

/**
 * Represents a file or a folder in a bucket. If the item is a folder, everything except [name] is null.
 */
@Serializable
data class BucketItem(
    val name: String,
    val id: String?,
    @SerialName("updated_at")
    val updatedAt: Instant?,
    @SerialName("created_at")
    val createdAt: Instant?,
    @SerialName("last_accessed_at")
    val lastAccessedAt: Instant?,
    val metadata: JsonObject?
)