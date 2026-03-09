package io.github.jan.supabase.storage.analytics

import io.github.jan.supabase.auth.AuthenticatedSupabaseApi
import io.github.jan.supabase.safeBody
import io.github.jan.supabase.storage.StorageListFilter
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.contentOrNull
import kotlinx.serialization.json.jsonPrimitive
import kotlinx.serialization.json.put

interface StorageAnalyticsClient {

    suspend fun createBucket(name: String): AnalyticBucket

    suspend fun listBuckets(filter: StorageListFilter.Buckets.() -> Unit = {}): List<AnalyticBucket>

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