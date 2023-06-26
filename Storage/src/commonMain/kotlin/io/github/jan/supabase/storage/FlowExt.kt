package io.github.jan.supabase.storage

import io.github.jan.supabase.exceptions.HttpRequestException
import io.github.jan.supabase.exceptions.RestException
import io.ktor.client.call.body
import io.ktor.client.plugins.HttpRequestTimeoutException
import io.ktor.client.plugins.onDownload
import io.ktor.client.plugins.onUpload
import io.ktor.http.HttpMethod
import io.ktor.utils.io.ByteReadChannel
import io.ktor.utils.io.ByteWriteChannel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow

/**
 * Uploads a file in [bucketId] under [path]
 * @param path The path to upload the file to
 * @param upsert Whether to overwrite an existing file
 * @param data The data to upload
 * @return A flow that emits the upload progress and at last the key to the uploaded file
 * @throws RestException or one of its subclasses if receiving an error response
 * @throws HttpRequestTimeoutException if the request timed out
 * @throws HttpRequestException on network related issues
 */
fun BucketApi.updateAsFlow(path: String, data: UploadData, upsert: Boolean): Flow<UploadStatus> = callbackFlow {
    this@updateAsFlow as BucketApiImpl
    val key = uploadOrUpdate(HttpMethod.Put, bucketId, path, data, upsert) {
        onUpload { bytesSentTotal, contentLength ->
            trySend(UploadStatus.Progress(bytesSentTotal, contentLength))
        }
    }
    trySend(UploadStatus.Success(key))
    close()
}

/**
 * Uploads a file in [bucketId] under [path]
 * @param path The path to upload the file to
 * @param upsert Whether to overwrite an existing file
 * @param data The data to upload
 * @return A flow that emits the upload progress and at last the key to the uploaded file
 * @throws RestException or one of its subclasses if receiving an error response
 * @throws HttpRequestTimeoutException if the request timed out
 * @throws HttpRequestException on network related issues
 */
fun BucketApi.uploadAsFlow(path: String, data: ByteArray, upsert: Boolean = false): Flow<UploadStatus> = uploadAsFlow(path, UploadData(
    ByteReadChannel(data), data.size.toLong()), upsert)

/**
 * Uploads a file in [bucketId] under [path] using a presigned url
 * @param path The path to upload the file to
 * @param token The presigned url token
 * @param data The data to upload
 * @param upsert Whether to overwrite an existing file
 * @return A flow that emits the upload progress and at last the key to the uploaded file
 * @throws RestException or one of its subclasses if receiving an error response
 * @throws HttpRequestTimeoutException if the request timed out
 * @throws HttpRequestException on network related issues
 */
fun BucketApi.uploadToSignedUrlAsFlow(
    path: String,
    token: String,
    data: UploadData,
    upsert: Boolean
): Flow<UploadStatus> {
    return callbackFlow {
        this@uploadToSignedUrlAsFlow as BucketApiImpl
        val key = uploadToSignedUrl(path, token, data, upsert) {
            onUpload { bytesSentTotal, contentLength ->
                trySend(UploadStatus.Progress(bytesSentTotal, contentLength))
            }
        }
        trySend(UploadStatus.Success(key))
        close()
    }
}

/**
 * Uploads a file in [bucketId] under [path] using a presigned url
 * @param path The path to upload the file to
 * @param token The presigned url token
 * @param data The data to upload
 * @param upsert Whether to overwrite an existing file
 * @return A flow that emits the upload progress and at last the key to the uploaded file
 */
fun BucketApi.uploadToSignedUrlAsFlow(path: String, token: String, data: ByteArray, upsert: Boolean = false): Flow<UploadStatus> = uploadToSignedUrlAsFlow(path, token, UploadData(ByteReadChannel(data), data.size.toLong()), upsert)

/**
 * Updates a file in [bucketId] under [path]
 * @param path The path to update the file to
 * @param data The new data
 * @param upsert Whether to overwrite an existing file
 * @return A flow that emits the upload progress and at last the key to the uploaded file
 * @throws RestException or one of its subclasses if receiving an error response
 * @throws HttpRequestTimeoutException if the request timed out
 * @throws HttpRequestException on network related issues
 */
fun BucketApi.uploadAsFlow(path: String, data: UploadData, upsert: Boolean): Flow<UploadStatus> {
    return callbackFlow {
        this@uploadAsFlow as BucketApiImpl
        val key = uploadOrUpdate(HttpMethod.Post, bucketId, path, data, upsert) {
            onUpload { bytesSentTotal, contentLength ->
                trySend(UploadStatus.Progress(bytesSentTotal, contentLength))
            }
        }
        trySend(UploadStatus.Success(key))
        close()
    }
}

/**
 * Updates a file in [bucketId] under [path]
 * @param path The path to update the file to
 * @param data The new data
 * @param upsert Whether to overwrite an existing file
 * @return A flow that emits the upload progress and at last the key to the uploaded file
 * @throws RestException or one of its subclasses if receiving an error response
 * @throws HttpRequestTimeoutException if the request timed out
 * @throws HttpRequestException on network related issues
 */
fun BucketApi.updateAsFlow(path: String, data: ByteArray, upsert: Boolean = false): Flow<UploadStatus> = updateAsFlow(path, UploadData(ByteReadChannel(data), data.size.toLong()), upsert)

/**
 * Downloads a file from [bucketId] under [path]
 * @param path The path to download
 * @param transform The transformation to apply to the image
 * @return A flow that emits the download progress and at last the data as a byte array
 * @throws RestException or one of its subclasses if receiving an error response
 * @throws HttpRequestTimeoutException if the request timed out
 * @throws HttpRequestException on network related issues
 */
fun BucketApi.downloadAuthenticatedAsFlow(
    path: String,
    transform: ImageTransformation.() -> Unit
): Flow<DownloadStatus> {
    return callbackFlow {
        this@downloadAuthenticatedAsFlow as BucketApiImpl
        val data = storage.api.rawRequest {
            prepareDownloadRequest(path, false, transform)
            onDownload { bytesSentTotal, contentLength ->
                trySend(DownloadStatus.Progress(bytesSentTotal, contentLength))
            }
        }.body<ByteArray>()
        trySend(DownloadStatus.Success)
        trySend(DownloadStatus.ByteData(data))
        close()
    }
}

/**
 * Downloads a file from [bucketId] under [path]
 * @param path The path to download
 * @param channel The channel to write the data to
 * @param transform The transformation to apply to the image
 * @return A flow that emits the download progress and at last the data as a byte array
 * @throws RestException or one of its subclasses if receiving an error response
 * @throws HttpRequestTimeoutException if the request timed out
 * @throws HttpRequestException on network related issues
 */
fun BucketApi.downloadAuthenticatedAsFlow(
    path: String,
    channel: ByteWriteChannel,
    transform: ImageTransformation.() -> Unit
): Flow<DownloadStatus> {
    this as BucketApiImpl
    return flowChannelDownloadRequest(path, channel, false, transform)
}

/**
 * Downloads a file from [bucketId] under [path] using the public url
 * @param path The path to download
 * @param transform The transformation to apply to the image
 * @return A flow that emits the download progress and at last the data as a byte array
 * @throws RestException or one of its subclasses if receiving an error response
 * @throws HttpRequestTimeoutException if the request timed out
 * @throws HttpRequestException on network related issues
 */
fun BucketApi.downloadPublicAsFlow(path: String, transform: ImageTransformation.() -> Unit): Flow<DownloadStatus> {
    return callbackFlow {
        this@downloadPublicAsFlow as BucketApiImpl
        val data = storage.api.rawRequest {
            prepareDownloadRequest(path, true, transform)
            onDownload { bytesSentTotal, contentLength ->
                trySend(DownloadStatus.Progress(bytesSentTotal, contentLength))
            }
        }.body<ByteArray>()
        trySend(DownloadStatus.Success)
        trySend(DownloadStatus.ByteData(data))
        close()
    }
}

/**
 * Downloads a file from [bucketId] under [path] using the public url
 * @param path The path to download
 * @param channel The channel to write the data to
 * @param transform The transformation to apply to the image
 * @return A flow that emits the download progress and at last the data as a byte array
 * @throws RestException or one of its subclasses if receiving an error response
 * @throws HttpRequestTimeoutException if the request timed out
 * @throws HttpRequestException on network related issues
 */
fun BucketApi.downloadPublicAsFlow(
    path: String,
    channel: ByteWriteChannel,
    transform: ImageTransformation.() -> Unit
): Flow<DownloadStatus> {
    this as BucketApiImpl
    return flowChannelDownloadRequest(path, channel, true, transform)
}

private fun BucketApiImpl.flowChannelDownloadRequest(
    path: String,
    channel: ByteWriteChannel,
    public: Boolean,
    transform: ImageTransformation.() -> Unit
): Flow<DownloadStatus> = callbackFlow {
    channelDownloadRequest(path, channel, public, transform) {
        onDownload { bytesSentTotal, contentLength ->
            trySend(DownloadStatus.Progress(bytesSentTotal, contentLength))
        }
    }
    trySend(DownloadStatus.Success)
    close()
}