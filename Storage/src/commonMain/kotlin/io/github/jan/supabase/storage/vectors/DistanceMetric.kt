package io.github.jan.supabase.storage.vectors

/**
 * Distance metrics for vector similarity search
 */
enum class DistanceMetric {
    COSINE, EUCLIDEAN, DOTPRODUCT;

    val value = this.name.lowercase()
}