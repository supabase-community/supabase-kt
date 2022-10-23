package io.github.jan.supabase.storage

import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.exceptions.RestException
import io.github.jan.supabase.gotrue.GoTrue
import io.github.jan.supabase.plugins.MainConfig
import io.github.jan.supabase.plugins.MainPlugin
import io.github.jan.supabase.plugins.SupabasePluginProvider
import io.ktor.client.call.body
import io.ktor.client.request.HttpRequestBuilder
import io.ktor.client.request.headers
import io.ktor.client.request.request
import io.ktor.client.request.setBody
import io.ktor.http.ContentType
import io.ktor.http.HttpHeaders
import io.ktor.http.HttpMethod
import io.ktor.http.contentType
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.int
import kotlinx.serialization.json.jsonPrimitive
import kotlinx.serialization.json.put

sealed interface Storage : MainPlugin<Storage.Config> {

    /**
     * Creates a new bucket in the storage
     * @param name the name of the bucket
     * @param id the id of the bucket
     * @param public whether the bucket should be public or not
     */
    suspend fun createBucket(name: String, id: String, public: Boolean)

    /**
     * Returns all buckets in the storage
     */
    suspend fun getAllBuckets(): List<Bucket>

    /**
     * Retrieves a bucket by its [id]
     */
    suspend fun getBucket(id: String): Bucket?

    /**
     * Changes a bucket's public status to [public]
     */
    suspend fun changePublicStatus(bucketId: String, public: Boolean)

    /**
     * Empties a bucket by its [bucketId]
     */
    suspend fun emptyBucket(bucketId: String)

    /**
     * Deletes a bucket by its [id]
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

    override suspend fun getAllBuckets(): List<Bucket> = makeRequest(HttpMethod.Get, "bucket").body()

    override suspend fun getBucket(id: String): Bucket? = makeRequest(HttpMethod.Get, "bucket/$id").body()

    override suspend fun deleteBucket(id: String) {
        makeRequest(HttpMethod.Delete, "bucket/$id")
    }

    override suspend fun createBucket(name: String, id: String, public: Boolean) {
        makeRequest(HttpMethod.Post, "bucket") {
            setBody(buildJsonObject {
                put("name", name)
                put("id", id)
                put("public", public)
            })
        }
    }

    override suspend fun changePublicStatus(bucketId: String, public: Boolean) {
        makeRequest(HttpMethod.Put, "bucket/$bucketId") {
            setBody(buildJsonObject {
                put("public", public)
            })
        }
    }

    override suspend fun emptyBucket(bucketId: String) {
        makeRequest(HttpMethod.Post, "bucket/$bucketId/empty")
    }

    override fun get(bucketId: String): BucketApi = BucketApiImpl(bucketId, this)

    suspend inline fun makeRequest(method: HttpMethod, path: String, json: Boolean = true, body: HttpRequestBuilder.() -> Unit = {}) = supabaseClient.httpClient.request(resolveUrl(path)) {
        this.method = method
        if(json) contentType(ContentType.Application.Json)
        addAuthorization()
        body()
    }.also {
        if(it.status.value == 400) {
            val error = it.body<JsonObject>()
            throw RestException(error["statusCode"]!!.jsonPrimitive.int, error["error"]!!.jsonPrimitive.content, error["message"]!!.jsonPrimitive.content)
        }
    }

    private fun HttpRequestBuilder.addAuthorization() {
        val token = config.jwtToken ?: supabaseClient.pluginManager.getPluginOrNull(GoTrue)?.currentAccessTokenOrNull()
        token.let {
            headers {
                append(HttpHeaders.Authorization, "Bearer $it")
            }
        }
    }

}

/**
 * Supabase Storage is a simple way to store large files for various purposes
 */
val SupabaseClient.storage: Storage
    get() = pluginManager.getPlugin(Storage)