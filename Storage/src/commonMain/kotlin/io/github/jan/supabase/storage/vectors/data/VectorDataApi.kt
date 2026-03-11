package io.github.jan.supabase.storage.vectors.data

import io.github.jan.supabase.auth.AuthenticatedSupabaseApi
import io.github.jan.supabase.safeBody
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.decodeFromJsonElement
import kotlinx.serialization.json.encodeToJsonElement
import kotlinx.serialization.json.put

/**
 * Interface for vector data operations.
 */
interface VectorDataApi {

    /**
     * Inserts or updates [vectors] in batch (1-500 per request)
     */
    suspend fun putVectors(vectors: List<VectorObject>)

    /**
     * Retrieves vectors by their keys in batch using the given [options]
     */
    suspend fun getVectors(options: GetVectorOptions.() -> Unit): List<VectorMatch>

    /**
     * Lists vectors in an index with pagination using the given [options]
     */
    suspend fun listVectors(options: ListVectorsOptions.() -> Unit = {}): ListVectorsResponse

    /**
     * Queries for similar vectors using approximate nearest neighbor search using the given [options]
     */
    suspend fun queryVectors(options: QueryVectorsOptions.() -> Unit): QueryVectorsResponse

    /**
     * Deletes vectors by their [keys] in batch (1-500 per request)
     */
    suspend fun deleteVectors(keys: List<String>)

}

internal class VectorDataApiImpl(
    private val bucketName: String,
    private val indexName: String,
    private val api: AuthenticatedSupabaseApi
): VectorDataApi {

    override suspend fun putVectors(vectors: List<VectorObject>) {
        require(vectors.size in 1..500) {
            "Vector batch size must be between 1 and 500 items"
        }
        api.postJson("PutVectors", buildJsonObject {
            put("vectorBucketName", bucketName)
            put("indexName", indexName)
            put("vectors", Json.encodeToJsonElement(vectors))
        })
    }

    override suspend fun getVectors(options: GetVectorOptions.() -> Unit): List<VectorMatch> {
        val options = GetVectorOptions(bucketName, indexName).apply(options)
        val body = api.postJson("GetVectors", options).safeBody<JsonObject>()
        return Json.decodeFromJsonElement(body["vectors"] ?: error("No value with key 'vectors': $body"))
    }

    override suspend fun listVectors(options: ListVectorsOptions.() -> Unit): ListVectorsResponse {
        val options = ListVectorsOptions(bucketName, indexName).apply(options)
        if(options.segmentCount != null) {
            require(options.segmentCount in 1..16) {
                "segmentCount must be between 1 and 16"
            }
            if(options.segmentIndex != null) {
                require(options.segmentIndex in 0..<options.segmentCount!!) {
                    "segmentIndex must be between 0 and ${options.segmentCount!! - 1}"
                }
            }
        }
        return api.postJson("ListVectors", options).safeBody()
    }

    override suspend fun queryVectors(options: QueryVectorsOptions.() -> Unit): QueryVectorsResponse {
        return api.postJson("QueryVectors", QueryVectorsOptions(bucketName, indexName).apply(options)).safeBody()
    }

    override suspend fun deleteVectors(keys: List<String>) {
        require(keys.size in 1..500) {
            "Keys batch size must be between 1 and 500 items"
        }
        api.postJson("DeleteVectors", buildJsonObject {
            put("vectorBucketName", bucketName)
            put("indexName", indexName)
            put("keys", Json.encodeToJsonElement(keys))
        })
    }

}