@file:OptIn(ExperimentalSettingsApi::class)

package io.github.jan.supabase.storage

import com.russhwolf.settings.ExperimentalSettingsApi
import io.github.aakira.napier.Napier
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.bodyOrNull
import io.github.jan.supabase.exceptions.*
import io.github.jan.supabase.gotrue.authenticatedSupabaseApi
import io.github.jan.supabase.plugins.MainConfig
import io.github.jan.supabase.plugins.MainPlugin
import io.github.jan.supabase.plugins.SupabasePluginProvider
import io.github.jan.supabase.safeBody
import io.github.jan.supabase.storage.resumable.ResumableCache
import io.ktor.client.plugins.HttpRequestTimeoutException
import io.ktor.client.statement.HttpResponse
import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put
import kotlin.time.Duration
import kotlin.time.Duration.Companion.seconds

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
     * @throws RestException or one of its subclasses if receiving an error response
     * @throws HttpRequestTimeoutException if the request timed out
     * @throws HttpRequestException on network related issues
     */
    suspend fun createBucket(name: String, id: String, builder: BucketBuilder.() -> Unit = {})

    /**
     * Returns all buckets in the storage
     * @throws RestException or one of its subclasses if receiving an error response
     * @throws HttpRequestTimeoutException if the request timed out
     * @throws HttpRequestException on network related issues
     */
    suspend fun retrieveBuckets(): List<Bucket>

    /**
     * Retrieves a bucket by its [id]
     * @throws RestException or one of its subclasses if receiving an error response
     * @throws HttpRequestTimeoutException if the request timed out
     * @throws HttpRequestException on network related issues
     */
    suspend fun retrieveBucketById(bucketId: String): Bucket?

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
    suspend fun deleteBucket(bucketId: String)

    /**
     * Builder function for interacting with a bucket with the given [bucketId]
     */
    operator fun get(bucketId: String): BucketApi

    /**
     * Builder function for interacting with a bucket with the given [bucketId]
     */
    fun from(bucketId: String): BucketApi = get(bucketId)

    /**
     * Config for the storage plugin
     * @param customUrl the custom url to use for the storage api
     * @param jwtToken the jwt token to use for the storage api
     */
    data class Config(
        override var customUrl: String? = null,
        override var jwtToken: String? = null,
        @PublishedApi internal var resumable: Resumable = Resumable()
    ) : MainConfig {

        /**
         * @param cache the cache for caching resumable upload urls
         * @param retryTimeout the timeout for retrying resumable uploads when uploading a chunk fails
         */
        data class Resumable(
            var cache: ResumableCache = ResumableCache.Memory(),
            var retryTimeout: Duration = 5.seconds
        ) {

            /**
             * The default chunk size for resumable uploads. **Supabase currently only supports a chunk size of 6MB, so be careful when changing this value**
             */
            var defaultChunkSize: Long = 6 * 1024 * 1024
                set(value) {
                    if(value.toInt() != 6 * 1024 * 1024) {
                        Napier.w { "supabase currently only supports a chunk size of 6MB" }
                    }
                    field = value
                }

        }

        /**
         * Config for resumable uploads
         */
        inline fun resumable(builder: Resumable.() -> Unit) {
            resumable = Resumable().apply(builder)
        }

    }

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

    override suspend fun retrieveBuckets(): List<Bucket> = api.get("bucket").safeBody()

    override suspend fun retrieveBucketById(bucketId: String): Bucket? = api.get("bucket/$bucketId").safeBody()

    override suspend fun deleteBucket(bucketId: String) {
        api.delete("bucket/$bucketId")
    }

    override suspend fun createBucket(name: String, id: String, builder: BucketBuilder.() -> Unit) {
        val bucketBuilder = BucketBuilder().apply(builder)
        val body = buildJsonObject {
            put("name", name)
            put("id", id)
            put("public", bucketBuilder.public)
            bucketBuilder.allowedMimeTypes?.let {
                put("allowed_mime_types", JsonArray(it.map { type -> JsonPrimitive(type) }))
            }
            bucketBuilder.fileSizeLimit?.let {
                put("file_size_limit", it.value)
            }
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
        val error = response.bodyOrNull<StorageErrorResponse>() ?: StorageErrorResponse(
            response.status.value,
            "Unknown error",
            ""
        )
        if (statusCode != 400) return UnknownRestException("Unknown error response", response)
        when (error.statusCode) {
            401 -> throw UnauthorizedRestException(error.error, response, error.message)
            400 -> throw BadRequestRestException(error.error, response, error.message)
            404 -> throw NotFoundRestException(error.error, response, error.message)
            else -> throw UnknownRestException(error.message, response)
        }
    }

}

/**
 * Supabase Storage is a simple way to store large files for various purposes
 */
val SupabaseClient.storage: Storage
    get() = pluginManager.getPlugin(Storage)