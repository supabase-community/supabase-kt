package io.github.jan.supabase.storage

import kotlinx.datetime.Instant
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonObject

/**
 * Represents a file or a folder in a bucket. If the item is a folder, everything except [name] is null.
 * @param name The name of the item
 * @param id The id of the item
 * @param updatedAt The last update date of the item
 * @param createdAt The creation date of the item
 * @param lastAccessedAt The last access date of the item
 * @param metadata The metadata of the item
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