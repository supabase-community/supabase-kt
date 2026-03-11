package io.github.jan.supabase.storage.vectors.index

import io.github.jan.supabase.dsl.required
import io.github.jan.supabase.storage.vectors.DistanceMetric
import kotlinx.serialization.Serializable

/**
 * Options for creating a vector index
 * TODO: Docs
 */
@Serializable
class CreateIndexOptions(val vectorBucketName: String) {

    var indexName: String by required()
    var dataType: VectorDataType by required()
    var dimension: Int by required()
    var distanceMetric: DistanceMetric by required()
    var metadataConfiguration: MetadataConfiguration? = null

}
