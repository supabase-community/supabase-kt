package io.github.jan.supabase.storage.vectors

import io.github.jan.supabase.auth.AuthenticatedSupabaseApi
import io.github.jan.supabase.safeBody
import io.github.jan.supabase.storage.vectors.bucket.ListVectorBucketsResponse
import io.github.jan.supabase.storage.vectors.bucket.VectorBucket
import io.github.jan.supabase.storage.vectors.bucket.VectorBucketFilter
import io.github.jan.supabase.storage.vectors.index.VectorIndexApi
import io.github.jan.supabase.storage.vectors.index.VectorIndexApiImpl
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.decodeFromJsonElement
import kotlinx.serialization.json.put

/**
 * Interface for vector bucket operations
 */
interface StorageVectorsClient {

    /**
     * Creates a new vector bucket with the given [name]
     */
    suspend fun createBucket(name: String)

    /**
     * Retrieves metadata for a specific vector bucket
     * @param name The unique bucket name
     */
    suspend fun getBucket(name: String): VectorBucket

    /**
     * Lists vector buckets with optional filtering and pagination
     * @param filter An optional filter
     */
    suspend fun listBuckets(filter: VectorBucketFilter.() -> Unit = {}): ListVectorBucketsResponse

    /**
     * Deletes a vector bucket (must be empty first)
     */
    suspend fun deleteBucket(name: String)

    /**
     * Access operations for a specific vector bucket
     * Returns a scoped client for index and vector operations within the bucket
     *
     * **Public alpha:** This API is part of a public alpha release and may not be available to your account type.
     *
     * @param vectorBucketName Name of the vector bucket
     * @returns Bucket-scoped client with index and vector operations
     */
    fun from(vectorBucketName: String): VectorIndexApi

}

internal class StorageVectorsClientImpl(
    private val api: AuthenticatedSupabaseApi
): StorageVectorsClient {

    override suspend fun createBucket(name: String) {
        api.postJson("CreateVectorBucket", buildJsonObject {
            put("vectorBucketName", name)
        })
    }

    override suspend fun getBucket(name: String): VectorBucket {
        val responseBody = api.postJson("GetVectorBucket", buildJsonObject {
            put("vectorBucketName", name)
        }).safeBody<JsonObject>()
        return Json.decodeFromJsonElement(responseBody["vectorBucket"] ?: error("No vectorBucket found in $responseBody"))
    }

    override suspend fun listBuckets(filter: VectorBucketFilter.() -> Unit): ListVectorBucketsResponse {
        val filter = VectorBucketFilter().apply(filter).build()
        return api.postJson("ListVectorBuckets", filter).safeBody()
    }

    override suspend fun deleteBucket(name: String) {
        api.postJson("DeleteVectorBucket", buildJsonObject {
            put("vectorBucketName", name)
        })
    }

    override fun from(vectorBucketName: String): VectorIndexApi {
        return VectorIndexApiImpl(vectorBucketName, api)
    }

}