package io.github.jan.supabase.storage

import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.bodyOrNull
import io.github.jan.supabase.exceptions.BadRequestRestException
import io.github.jan.supabase.exceptions.HttpRequestException
import io.github.jan.supabase.exceptions.NotFoundRestException
import io.github.jan.supabase.exceptions.RestException
import io.github.jan.supabase.exceptions.UnauthorizedRestException
import io.github.jan.supabase.exceptions.UnknownRestException
import io.github.jan.supabase.gotrue.authenticatedSupabaseApi
import io.github.jan.supabase.plugins.MainConfig
import io.github.jan.supabase.plugins.MainPlugin
import io.github.jan.supabase.plugins.SupabasePluginProvider
import io.github.jan.supabase.safeBody
import io.ktor.client.plugins.HttpRequestTimeoutException
import io.ktor.client.statement.HttpResponse
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put

/**
 * Plugin for interacting with the supabase storage api
 *
 * To use it you need to install it to the [SupabaseClient]:
 * ```kotlin
 * val client = createSupabaseClient(supabaseUrl, supabaseKey) {
 *    install(Storage)
 * }
 * ```
 *
 * then you have to interact with the storage api:
 * ```kotlin
 * val bucket = client.storage["icons"]
 * val bytes = bucket.downloadAuthenticated("icon.png")
 * ```
 */
sealed interface Storage : MainPlugin<Storage.Config> {

    /**
     * Creates a new bucket in the storage
     * @param name the name of the bucket
     * @param id the id of the bucket
     * @param public whether the bucket should be public or not
     * @throws RestException or one of its subclasses if receiving an error response
     * @throws HttpRequestTimeoutException if the request timed out
     * @throws HttpRequestException on network related issues
     */
    suspend fun createBucket(name: String, id: String, public: Boolean)

    /**
     * Returns all buckets in the storage
     * @throws RestException or one of its subclasses if receiving an error response
     * @throws HttpRequestTimeoutException if the request timed out
     * @throws HttpRequestException on network related issues
     */
    suspend fun getAllBuckets(): List<Bucket>

    /**
     * Retrieves a bucket by its [id]
     * @throws RestException or one of its subclasses if receiving an error response
     * @throws HttpRequestTimeoutException if the request timed out
     * @throws HttpRequestException on network related issues
     */
    suspend fun getBucket(id: String): Bucket?

    /**
     * Changes a bucket's public status to [public]
     * @throws RestException or one of its subclasses if receiving an error response
     * @throws HttpRequestTimeoutException if the request timed out
     * @throws HttpRequestException on network related issues
     */
    suspend fun changePublicStatus(bucketId: String, public: Boolean)

    /**
     * Empties a bucket by its [bucketId]
     * @throws RestException or one of its subclasses if receiving an error response
     * @throws HttpRequestTimeoutException if the request timed out
     * @throws HttpRequestException on network related issues
     */
    suspend fun emptyBucket(bucketId: String)

    /**
     * Deletes a bucket by its [id]
     * @throws RestException or one of its subclasses if receiving an error response
     * @throws HttpRequestTimeoutException if the request timed out
     * @throws HttpRequestException on network related issues
     */
    suspend fun deleteBucket(id: String)

    operator fun get(bucketId: String): BucketApi

    fun from(id: String): BucketApi = get(id)

    data class Config(override var customUrl: String? = null, override var jwtToken: String? = null): MainConfig

    companion object : SupabasePluginProvider<Config, Storage> {

        override val key: String = "storage"
        const val API_VERSION = 1

        override fun createConfig(init: Config.() -> Unit) = Config().apply(init)
        override fun create(supabaseClient: SupabaseClient, config: Config): Storage {
            return StorageImpl(supabaseClient, config)
        }

    }

}

internal class StorageImpl(override val supabaseClient: SupabaseClient, override val config: Storage.Config) : Storage {

    override val PLUGIN_KEY: String
        get() = Storage.key

    override val API_VERSION: Int
        get() = Storage.API_VERSION

    internal val api = supabaseClient.authenticatedSupabaseApi(this)

    override suspend fun getAllBuckets(): List<Bucket> = api.get("bucket").safeBody()

    override suspend fun getBucket(id: String): Bucket? = api.get("bucket/$id").safeBody()

    override suspend fun deleteBucket(id: String) {
        api.delete("bucket/$id")
    }

    override suspend fun createBucket(name: String, id: String, public: Boolean) {
        val body = buildJsonObject {
            put("name", name)
            put("id", id)
            put("public", public)
        }
        api.postJson("bucket", body)
    }

    override suspend fun changePublicStatus(bucketId: String, public: Boolean) {
        val body = buildJsonObject {
            put("public", public)
        }
        api.putJson("bucket/$bucketId", body)
    }

    override suspend fun emptyBucket(bucketId: String) {
        api.post("bucket/$bucketId/empty")
    }

    override fun get(bucketId: String): BucketApi = BucketApiImpl(bucketId, this)

    override suspend fun parseErrorResponse(response: HttpResponse): RestException {
        val statusCode = response.status.value
        val error = response.bodyOrNull<StorageErrorResponse>() ?: StorageErrorResponse(response.status.value, "Unknown error", "")
        if(statusCode != 400) return UnknownRestException("Unknown error response", response)
        when(error.statusCode) {
            401 -> throw UnauthorizedRestException(error.error, response, error.message)
            400 -> throw BadRequestRestException(error.error, response, error.message)
            404 -> throw NotFoundRestException(error.error, response, error.message)
            else -> throw UnknownRestException("Unknown error response", response)
        }
    }

}

/**
 * Supabase Storage is a simple way to store large files for various purposes
 */
val SupabaseClient.storage: Storage
    get() = pluginManager.getPlugin(Storage)