package io.github.jan.supabase.storage.analytics

import io.github.jan.supabase.auth.AuthenticatedSupabaseApi
import io.github.jan.supabase.safeBody
import io.github.jan.supabase.storage.StorageListFilter
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.contentOrNull
import kotlinx.serialization.json.jsonPrimitive
import kotlinx.serialization.json.put

/**
 * Interface for managing Analytics Buckets using Iceberg tables
 */
interface StorageAnalyticsClient {

    /**
     * Creates a new analytics bucket using Iceberg tables
     * Analytics buckets are optimized for analytical queries and data processing
     *
     * **Public alpha:** This API is part of a public alpha release and may not be available to your account type.
     *
     * @param name A unique name for the bucket you are creating
     */
    suspend fun createBucket(name: String): AnalyticBucket

    /**
     * Retrieves the details of all Analytics Storage buckets within an existing project
     * Only returns buckets of type 'ANALYTICS'
     *
     * **Public alpha:** This API is part of a public alpha release and may not be available to your account type.
     *
     * @param filter Query parameters for listing buckets
     */
    suspend fun listBuckets(filter: StorageListFilter.Buckets.() -> Unit = {}): List<AnalyticBucket>

    /**
     * Deletes an existing analytics bucket
     * A bucket can't be deleted, if it is not empty
     * You must first empty the bucket before deletion
     *
     * **Public alpha:** This API is part of a public alpha release and may not be available to your account type.
     *
     * @param name The unique identifier of the bucket you would like to delete
     */
    suspend fun deleteBucket(name: String): String

    // TODO: from(); decide what to do here, maybe a full KMP wrapper?

}

internal class StorageAnalyticsClientImpl(
    private val api: AuthenticatedSupabaseApi
) : StorageAnalyticsClient {

    override suspend fun createBucket(name: String): AnalyticBucket {
        return api.postJson("", buildJsonObject {
            put("name", name)
        }).safeBody()
    }

    override suspend fun listBuckets(filter: StorageListFilter.Buckets.() -> Unit): List<AnalyticBucket> {
        val filter = StorageListFilter.Buckets().apply(filter)
        return api.get("bucket") {
            url.parameters.appendAll(filter.buildParameters())
        }.safeBody()
    }

    override suspend fun deleteBucket(name: String): String {
        return api.delete("bucket/$name").safeBody<JsonObject>()["message"]?.jsonPrimitive?.contentOrNull ?: error("Failed to delete bucket")
    }

}