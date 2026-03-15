package io.github.jan.supabase.storage.vectors

/**
 * Distance metrics for vector similarity search.
 *
 * @property value Lowercase API representation of the distance metric.
 */
enum class DistanceMetric {
    /** Cosine distance metric. */
    COSINE,

    /** Euclidean distance metric. */
    EUCLIDEAN,

    /** Dot product distance metric. */
    DOTPRODUCT;

    /** Lowercase API representation of the distance metric. */
    val value = this.name.lowercase()
}