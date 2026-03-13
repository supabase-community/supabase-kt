package io.github.jan.supabase.storage.vectors.index

import io.github.jan.supabase.dsl.required
import io.github.jan.supabase.storage.vectors.DistanceMetric
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.encodeToJsonElement
import kotlinx.serialization.json.put

/**
 * Options for creating a vector index
 * TODO: Docs
 */
class CreateIndexOptions(val vectorBucketName: String) {

    var indexName: String by required()
    var dataType: VectorDataType by required()
    var dimension: Int by required()
    var distanceMetric: DistanceMetric by required()
    var metadataConfiguration: MetadataConfiguration? = null

    internal fun build() = buildJsonObject {
        put("vectorBucketName", vectorBucketName)
        put("indexName", indexName)
        put("dataType", dataType.value)
        put("dimension", dimension)
        put("distanceMetric", distanceMetric.value)
        metadataConfiguration?.let { config ->
            put("metadataConfiguration", Json.encodeToJsonElement(config))
        }
    }

}
