package io.github.jan.supabase.storage

import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.exceptions.HttpRequestException
import io.github.jan.supabase.exceptions.RestException
import io.github.jan.supabase.gotrue.resolveAccessToken
import io.github.jan.supabase.storage.resumable.ResumableClient
import io.ktor.client.plugins.HttpRequestTimeoutException
import io.ktor.utils.io.ByteReadChannel
import io.ktor.utils.io.ByteWriteChannel
import kotlin.time.Duration

/**
 * The api for interacting with a bucket
 */
sealed interface BucketApi {

    /**
     * The id of the bucket
     */
    val bucketId: String

    /**
     * The current [SupabaseClient]
     */
    val supabaseClient: SupabaseClient

    /**
     * The client for interacting with the resumable upload api
     */
    val resumable: ResumableClient

    /**
     * Uploads a file in [bucketId] under [path]
     * @param path The path to upload the file to
     * @param data The data to upload
     * @return the key to the uploaded file
     * @throws IllegalArgumentException if data to upload is empty
     * @throws RestException or one of its subclasses if receiving an error response
     * @throws HttpRequestTimeoutException if the request timed out
     * @throws HttpRequestException on network related issues
     */
    suspend fun upload(path: String, data: ByteArray, options: FileOptionBuilder.() -> Unit = {}): FileUploadResponse {
        require(data.isNotEmpty()) { "The data to upload should not be empty" }
        return upload(path, UploadData(ByteReadChannel(data), data.size.toLong()), options)
    }

    /**
     * Uploads a file in [bucketId] under [path]
     * @param path The path to upload the file to
     * @param data The data to upload
     * @return the key to the uploaded file
     * @throws RestException or one of its subclasses if receiving an error response
     * @throws HttpRequestTimeoutException if the request timed out
     * @throws HttpRequestException on network related issues
     */
    suspend fun upload(path: String, data: UploadData, options: FileOptionBuilder.() -> Unit = {}): FileUploadResponse

    /**
     * Uploads a file in [bucketId] under [path] using a presigned url
     * @param path The path to upload the file to
     * @param token The pre-signed url token
     * @param data The data to upload
     * @return the key of the uploaded file
     * @throws IllegalArgumentException if data to upload is empty
     */
    suspend fun uploadToSignedUrl(
        path: String,
        token: String,
        data: ByteArray,
        options: FileOptionBuilder.() -> Unit = {}
    ): FileUploadResponse {
        require(data.isNotEmpty()) { "The data to upload should not be empty" }
        return uploadToSignedUrl(path, token, UploadData(ByteReadChannel(data), data.size.toLong()), options)
    }

    /**
     * Uploads a file in [bucketId] under [path] using a presigned url
     * @param path The path to upload the file to
     * @param token The presigned url token
     * @param data The data to upload
     * @return the key of the uploaded file
     * @throws RestException or one of its subclasses if receiving an error response
     * @throws HttpRequestTimeoutException if the request timed out
     * @throws HttpRequestException on network related issues
     * @throws HttpRequestException on network related issues
     */
    suspend fun uploadToSignedUrl(path: String, token: String, data: UploadData, options: FileOptionBuilder.() -> Unit = {}): FileUploadResponse

    /**
     * Updates a file in [bucketId] under [path]
     * @param path The path to update the file to
     * @param data The new data
     * @return the key to the updated file
     * @throws IllegalArgumentException if data to upload is empty
     * @throws RestException or one of its subclasses if receiving an error response
     * @throws HttpRequestTimeoutException if the request timed out
     * @throws HttpRequestException on network related issues
     */
    suspend fun update(path: String, data: ByteArray, options: FileOptionBuilder.() -> Unit = {}): FileUploadResponse {
        require(data.isNotEmpty()) { "The data to upload should not be empty" }
        return update(path, UploadData(ByteReadChannel(data), data.size.toLong()), options)
    }

    /**
     * Updates a file in [bucketId] under [path]
     * @param path The path to update the file to
     * @param data The new data
     * @return the key to the updated file
     * @throws RestException or one of its subclasses if receiving an error response
     * @throws HttpRequestTimeoutException if the request timed out
     * @throws HttpRequestException on network related issues
     */
    suspend fun update(path: String, data: UploadData, options: FileOptionBuilder.() -> Unit = {}): FileUploadResponse

    /**
     * Deletes all files in [bucketId] with in [paths]
     * @param paths The paths to delete
     * @throws RestException or one of its subclasses if receiving an error response
     * @throws HttpRequestTimeoutException if the request timed out
     * @throws HttpRequestException on network related issues
     */
    suspend fun delete(paths: Collection<String>)

    /**
     * Deletes all files in [bucketId] with in [paths]
     * @param paths The paths to delete
     * @throws RestException or one of its subclasses if receiving an error response
     * @throws HttpRequestTimeoutException if the request timed out
     * @throws HttpRequestException on network related issues
     */
    suspend fun delete(vararg paths: String) = delete(paths.toList())

    /**
     * Moves a file under [from] to [to]
     * @param from The path to move from
     * @param to The path to move to
     * @param destinationBucket The bucket to move the file to. If null, the file will be moved within the same bucket
     * @throws RestException or one of its subclasses if receiving an error response
     * @throws HttpRequestTimeoutException if the request timed out
     * @throws HttpRequestException on network related issues
     */
    suspend fun move(from: String, to: String, destinationBucket: String? = null)

    /**
     * Copies a file under [from] to [to]
     * @param from The path to copy from
     * @param to The path to copy to
     * @param destinationBucket The destination bucket to copy to. If null, the current bucket is used
     * @throws RestException or one of its subclasses if receiving an error response
     * @throws HttpRequestTimeoutException if the request timed out
     * @throws HttpRequestException on network related issues
     */
    suspend fun copy(from: String, to: String, destinationBucket: String? = null)

    /**
     * Creates a signed url to upload without authentication.
     * These urls are valid for 2 hours.
     * @param path The path to create an url for
     */
    suspend fun createSignedUploadUrl(path: String): UploadSignedUrl

    /**
     * Creates a signed url to download without authentication. The url will expire after [expiresIn]
     * @param path The path to create an url for
     * @param expiresIn The duration the url is valid
     * @param transform The transformation to apply to the image
     * @return The url to download the file
     * @throws RestException or one of its subclasses if receiving an error response
     * @throws HttpRequestTimeoutException if the request timed out
     * @throws HttpRequestException on network related issues
     */
    suspend fun createSignedUrl(path: String, expiresIn: Duration, transform: ImageTransformation.() -> Unit = {}): String

    /**
     * Creates signed urls for all specified paths. The urls will expire after [expiresIn]
     * @param expiresIn The duration the urls are valid
     * @param paths The paths to create urls for
     * @return A list of [SignedUrl]s
     * @throws RestException or one of its subclasses if receiving an error response
     * @throws HttpRequestTimeoutException if the request timed out
     * @throws HttpRequestException on network related issues
     */
    suspend fun createSignedUrls(expiresIn: Duration, paths: Collection<String>): List<SignedUrl>

    /**
     * Creates signed urls for all specified paths. The urls will expire after [expiresIn]
     * @param expiresIn The duration the urls are valid
     * @param paths The paths to create urls for
     * @return A list of [SignedUrl]s
     * @throws RestException or one of its subclasses if receiving an error response
     * @throws HttpRequestTimeoutException if the request timed out
     * @throws HttpRequestException on network related issues
     */
    suspend fun createSignedUrls(expiresIn: Duration, vararg paths: String) = createSignedUrls(expiresIn, paths.toList())

    /**
     * Downloads a file from [bucketId] under [path]
     * @param path The path to download
     * @param transform The transformation to apply to the image
     * @return The file as a byte array
     * @throws RestException or one of its subclasses if receiving an error response
     * @throws HttpRequestTimeoutException if the request timed out
     * @throws HttpRequestException on network related issues
     */
    suspend fun downloadAuthenticated(path: String, transform: ImageTransformation.() -> Unit = {}): ByteArray

    /**
     * Downloads a file from [bucketId] under [path]
     * @param path The path to download
     * @param channel The channel to write the data to
     * @param transform The transformation to apply to the image
     * @throws RestException or one of its subclasses if receiving an error response
     * @throws HttpRequestTimeoutException if the request timed out
     * @throws HttpRequestException on network related issues
     */
    suspend fun downloadAuthenticated(path: String, channel: ByteWriteChannel, transform: ImageTransformation.() -> Unit = {})

    /**
     * Downloads a file from [bucketId] under [path] using the public url
     * @param path The path to download
     * @param transform The transformation to apply to the image
     * @return The file as a byte array
     * @throws RestException or one of its subclasses if receiving an error response
     * @throws HttpRequestTimeoutException if the request timed out
     * @throws HttpRequestException on network related issues
     */
    suspend fun downloadPublic(path: String, transform: ImageTransformation.() -> Unit = {}): ByteArray

    /**
     * Downloads a file from [bucketId] under [path] using the public url
     * @param path The path to download
     * @param channel The channel to write the data to
     * @param transform The transformation to apply to the image
     * @throws RestException or one of its subclasses if receiving an error response
     * @throws HttpRequestTimeoutException if the request timed out
     * @throws HttpRequestException on network related issues
     */
    suspend fun downloadPublic(path: String, channel: ByteWriteChannel, transform: ImageTransformation.() -> Unit = {})


    /**
     * Searches for files with the given [prefix] and [filter]
     * @throws RestException or one of its subclasses if receiving an error response
     * @throws HttpRequestTimeoutException if the request timed out
     * @throws HttpRequestException on network related issues
     */
    suspend fun list(
        prefix: String = "",
        filter: BucketListFilter.() -> Unit = {}
    ): List<FileObject>

    /**
     * Returns information about the file under [path]
     * @param path The path to get information about
     * @return The file object
     * @throws RestException or one of its subclasses if receiving an error response
     * @throws HttpRequestTimeoutException if the request timed out
     * @throws HttpRequestException on network related issues
     */
    suspend fun info(path: String): FileObjectV2

    /**
     * Checks if a file exists under [path]
     * @return true if the file exists, false otherwise
     * @throws RestException or one of its subclasses if receiving an error response
     * @throws HttpRequestTimeoutException if the request timed out
     * @throws HttpRequestException on network related issues
     */
    suspend fun exists(path: String): Boolean

    /**
     * Changes the bucket's public status to [public]
     * @throws RestException or one of its subclasses if receiving an error response
     * @throws HttpRequestTimeoutException if the request timed out
     * @throws HttpRequestException on network related issues
     */
    suspend fun changePublicStatusTo(public: Boolean)

    /**
     * Returns the public url of [path]
     * @throws RestException or one of its subclasses if receiving an error response
     * @throws HttpRequestTimeoutException if the request timed out
     * @throws HttpRequestException on network related issues
     */
    fun publicUrl(path: String): String

    /**
     * Returns the authenticated url of [path]. Requires bearer token authentication using the user's access token
     * @throws RestException or one of its subclasses if receiving an error response
     * @throws HttpRequestTimeoutException if the request timed out
     * @throws HttpRequestException on network related issues
     */
    fun authenticatedUrl(path: String): String

    /**
     * Returns the authenticated render url of [path] with the given [transform]
     * @throws RestException or one of its subclasses if receiving an error response
     * @throws HttpRequestTimeoutException if the request timed out
     * @throws HttpRequestException on network related issues
     */
    fun authenticatedRenderUrl(path: String, transform: ImageTransformation.() -> Unit = {}): String

    /**
     * Returns the public render url of [path] with the given [transform]
     * @throws RestException or one of its subclasses if receiving an error response
     * @throws HttpRequestTimeoutException if the request timed out
     * @throws HttpRequestException on network related issues
     */
    fun publicRenderUrl(path: String, transform: ImageTransformation.() -> Unit = {}): String

    companion object {

        /**
         * The header to use for upserting files
         */
        const val UPSERT_HEADER = "x-upsert"

    }

}


/**
 * Can be used if you want to quickly access a file under an **url** with your **auth_token** using a custom download method.
 *
 *
 * To interact with files which require authentication use the provided access token and add it to the Authorization header:
 *
 * **Authentication: Bearer <your_access_token>**
 * @param path The path to download
 */
suspend fun BucketApi.authenticatedRequest(path: String): Pair<String, String> {
    val url = authenticatedUrl(path)
    val token = supabaseClient.resolveAccessToken(supabaseClient.storage) ?: error("No access token available")
    return token to url
}