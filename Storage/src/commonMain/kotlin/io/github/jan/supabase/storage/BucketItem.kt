package io.github.jan.supabase.storage

import io.github.jan.supabase.SupabaseSerializer
import io.github.jan.supabase.decode
import io.github.jan.supabase.serializer.KotlinXSerializer
import kotlinx.datetime.Instant
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.Transient
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
//TODO: Rename to FileObject
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
data class FileObjectV2(
    val name: String,
    val id: String?,
    val version: String,
    @SerialName("bucket_id")
    val bucketId: String? = null,
    @SerialName("updated_at")
    val updatedAt: Instant? = null,
    @SerialName("created_at")
    val createdAt: Instant?,
    @SerialName("last_accessed_at")
    val lastAccessedAt: Instant? = null,
    val metadata: JsonObject?,
    val size: Long,
    @SerialName("content_type")
    val contentType: String,
    val etag: String?,
    @SerialName("last_modified")
    val lastModified: Instant?,
    @SerialName("cache_control")
    val cacheControl: String?,
    @Transient @PublishedApi internal val serializer: SupabaseSerializer = KotlinXSerializer()
) {

    inline fun <reified T> decodeMetadata(): T? = metadata?.let { serializer.decode(it.toString()) }

}