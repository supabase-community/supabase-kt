package io.github.jan.supacompose.storage


import kotlinx.datetime.Instant
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class Bucket(
    @SerialName("created_at")
    val createdAt: Instant,
    @SerialName("id")
    val id: String,
    @SerialName("name")
    val name: String,
    @SerialName("owner")
    val owner: String,
    @SerialName("updated_at")
    val updatedAt: Instant
)