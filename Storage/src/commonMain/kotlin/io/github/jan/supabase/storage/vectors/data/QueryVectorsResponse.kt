package io.github.jan.supabase.storage.vectors.data

import io.github.jan.supabase.storage.vectors.DistanceMetric
import kotlinx.serialization.Serializable

/**
 * Response from vector similarity query
 * @property vectors - Array of similar vectors ordered by distance
 * @property distanceMetric - The distance metric used for the similarity search
 */
@Serializable
class QueryVectorsResponse(
    val vectors: List<VectorMatch>,
    val distanceMetric: DistanceMetric? = null
)