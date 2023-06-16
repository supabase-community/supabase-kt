package io.github.jan.supabase.storage

import co.touchlab.kermit.Logger
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.annotations.SupabaseInternal
import io.github.jan.supabase.bodyOrNull
import io.github.jan.supabase.collections.AtomicMutableMap
import io.github.jan.supabase.exceptions.BadRequestRestException
import io.github.jan.supabase.exceptions.NotFoundRestException
import io.github.jan.supabase.exceptions.RestException
import io.github.jan.supabase.exceptions.UnauthorizedRestException
import io.github.jan.supabase.exceptions.UnknownRestException
import io.github.jan.supabase.gotrue.authenticatedSupabaseApi
import io.github.jan.supabase.plugins.MainConfig
import io.github.jan.supabase.plugins.MainPlugin
import io.github.jan.supabase.plugins.SupabasePluginProvider
import io.github.jan.supabase.safeBody
import io.github.jan.supabase.storage.resumable.ResumableCache
import io.ktor.client.plugins.HttpRequestTimeoutException
import io.ktor.client.plugins.timeout
import io.ktor.client.statement.HttpResponse
import io.ktor.http.HttpStatusCode
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
    suspend fun createBucket(id: String, builder: BucketBuilder.() -> Unit = {})

    /**
     * Updates a bucket in the storage
     * @param id the id of the bucket
     * @param builder the builder for the bucket
     */
    suspend fun updateBucket(id: String, builder: BucketBuilder.() -> Unit = {})

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
    @Deprecated("use updateBucket instead", ReplaceWith("updateBucket(bucketId) { \nthis@updateBucket.public = public\n }"))
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
     * @param transferTimeout the timeout for uploading and downloading files (default: 120 seconds)
     */
    data class Config(
        override var customUrl: String? = null,
        override var jwtToken: String? = null,
        var transferTimeout: Duration = 120.seconds,
        @PublishedApi internal var resumable: Resumable = Resumable()
    ) : MainConfig {

        /**
         * @param cache the cache for caching resumable upload urls
         * @param retryTimeout the timeout for retrying resumable uploads when uploading a chunk fails
         * @param onlyUpdateStateAfterChunk whether the state should only be updated after a chunk was uploaded successfully or also when the chunk is currently being uploaded
         */
        data class Resumable(
            var cache: ResumableCache = ResumableCache.Disk(),
            var retryTimeout: Duration = 5.seconds,
            var onlyUpdateStateAfterChunk: Boolean = false
        ) {

            /**
             * The default chunk size for resumable uploads. **Supabase currently only supports a chunk size of 6MB, so be careful when changing this value**
             */
            var defaultChunkSize: Long = DEFAULT_CHUNK_SIZE
                set(value) {
                    if(value != DEFAULT_CHUNK_SIZE) {
                        Logger.w { "supabase currently only supports a chunk size of 6MB" }
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

        /**
         * The api version of the storage plugin
         */
        const val API_VERSION = 1

        /**
         * The default chunk size for resumable uploads.
         */
        const val DEFAULT_CHUNK_SIZE = 6L * 1024L * 1024L

        override fun createConfig(init: Config.() -> Unit) = Config().apply(init)
        override fun create(supabaseClient: SupabaseClient, config: Config): Storage {
            return StorageImpl(supabaseClient, config)
        }

    }

}

internal class StorageImpl(override val supabaseClient: SupabaseClient, override val config: Storage.Config) : Storage {

    override val pluginKey: String
        get() = Storage.key

    override val apiVersion: Int
        get() = Storage.API_VERSION

    @OptIn(SupabaseInternal::class)
    internal val api = supabaseClient.authenticatedSupabaseApi(this) {
        timeout {
            requestTimeoutMillis = config.transferTimeout.inWholeMilliseconds
        }
    }
    private val resumableClients = AtomicMutableMap<String, BucketApi>()

    override suspend fun retrieveBuckets(): List<Bucket> = api.get("bucket").safeBody()

    override suspend fun retrieveBucketById(bucketId: String): Bucket? = api.get("bucket/$bucketId").safeBody()

    override suspend fun deleteBucket(bucketId: String) {
        api.delete("bucket/$bucketId")
    }

    override suspend fun createBucket(id: String, builder: BucketBuilder.() -> Unit) {
        val bucketBuilder = BucketBuilder().apply(builder)
        val body = buildJsonObject {
            put("name", id)
            put("id", id)
            put("public", bucketBuilder.public ?: false)
            bucketBuilder.allowedMimeTypes?.let {
                put("allowed_mime_types", JsonArray(it.map { type -> JsonPrimitive(type) }))
            }
            bucketBuilder.fileSizeLimit?.let {
                put("file_size_limit", it.value)
            }
        }
        api.postJson("bucket", body)
    }

    override suspend fun updateBucket(id: String, builder: BucketBuilder.() -> Unit) {
        val bucketBuilder = BucketBuilder().apply(builder)
        val body = buildJsonObject {
            put("name", id)
            put("id", id)
            bucketBuilder.public?.let {
                put("public", bucketBuilder.public)
            }
            bucketBuilder.allowedMimeTypes?.let {
                put("allowed_mime_types", JsonArray(it.map { type -> JsonPrimitive(type) }))
            }
            bucketBuilder.fileSizeLimit?.let {
                put("file_size_limit", it.value)
            }
        }
        api.putJson("bucket/$id", body)
    }

    @Deprecated("use updateBucket instead", ReplaceWith("updateBucket(bucketId) { \nthis@updateBucket.public = public\n }"))
    override suspend fun changePublicStatus(bucketId: String, public: Boolean) {
        val body = buildJsonObject {
            put("public", public)
        }
        api.putJson("bucket/$bucketId", body)
    }

    override suspend fun emptyBucket(bucketId: String) {
        api.post("bucket/$bucketId/empty")
    }

    override fun get(bucketId: String): BucketApi = resumableClients.getOrPut(bucketId) {
        BucketApiImpl(bucketId, this)
    }

    override suspend fun parseErrorResponse(response: HttpResponse): RestException {
        val statusCode = response.status
        val error = response.bodyOrNull<StorageErrorResponse>() ?: StorageErrorResponse(
            response.status.value,
            "Unknown error",
            ""
        )
        if (statusCode != HttpStatusCode.BadRequest) return UnknownRestException("Unknown error response $error", response)
        when (error.statusCode) {
            HttpStatusCode.Unauthorized.value -> throw UnauthorizedRestException(error.error, response, error.message)
            HttpStatusCode.BadRequest.value -> throw BadRequestRestException(error.error, response, error.message)
            HttpStatusCode.NotFound.value -> throw NotFoundRestException(error.error, response, error.message)
            else -> throw UnknownRestException(error.message, response)
        }
    }

}

/**
 * Supabase Storage is a simple way to store large files for various purposes
 */
val SupabaseClient.storage: Storage
    get() = pluginManager.getPlugin(Storage)
