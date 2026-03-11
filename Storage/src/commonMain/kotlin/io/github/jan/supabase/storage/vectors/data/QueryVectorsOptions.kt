package io.github.jan.supabase.storage.vectors.data

import io.github.jan.supabase.dsl.required
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonObject

/**
 * Options for querying similar vectors (ANN search)
 * @property vectorBucketName Name of the vector bucket
 * @property indexName Name of the index
 * @property queryVector Query vector to find similar vectors
 * @property topK Number of nearest neighbors to return (default: 10)
 * @property filter Optional JSON filter for metadata
 * @property returnDistance Whether to include distance scores
 * @property returnMetadata Whether to include metadata in results
 */
@Serializable
class QueryVectorsOptions(
    val vectorBucketName: String,
    val indexName: String
) {

    var queryVector: VectorData by required()
    var topK: Int? = null
    var filter: JsonObject? = null
    var returnDistance: Boolean? = null
    var returnMetadata: Boolean? = null

}