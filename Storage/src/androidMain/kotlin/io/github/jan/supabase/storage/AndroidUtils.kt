package io.github.jan.supabase.storage

import android.net.Uri
import io.ktor.utils.io.ByteReadChannel
import io.ktor.utils.io.jvm.javaio.toByteReadChannel

/**
 * Uploads a file in [BucketApi.bucketId] under [path]
 * @param path The path to upload the file to
 * @param uri The uri to upload
 * @param upsert Whether to overwrite an existing file
 * @return the key to the updated file
 */
suspend fun BucketApi.upload(path: String, uri: Uri, upsert: Boolean = false) = upload(path, UploadData(uri.readChannel(), uri.contentSize), upsert)

/**
 * Uploads a file in [BucketApi.bucketId] under [path]
 * @param path The path to upload the file to
 * @param uri The uri to upload
 * @param upsert Whether to overwrite an existing file
 * @return A flow that emits the upload progress and at last the key to the updated file
 */
fun BucketApi.uploadAsFlow(path: String, uri: Uri, upsert: Boolean = false) = uploadAsFlow(path, UploadData(uri.readChannel(), uri.contentSize), upsert)

/**
 * Uploads a file in [BucketApi.bucketId] under [path] using a presigned url
 * @param path The path to upload the file to
 * @param token The presigned url token
 * @param uri The uri to upload
 * @return the key to the updated file
 */
suspend fun BucketApi.uploadToSignedUrl(path: String, token: String, uri: Uri, upsert: Boolean = false) = uploadToSignedUrl(path, token, UploadData(uri.readChannel(), uri.contentSize), upsert)

/**
 * Uploads a file in [BucketApi.bucketId] under [path] using a presigned url
 * @param path The path to upload the file to
 * @param token The presigned url token
 * @param uri The uri to upload
 * @param upsert Whether to overwrite an existing file
 * @return A flow that emits the upload progress and at last the key to the updated file
 */
fun BucketApi.uploadToSignedUrlAsFlow(path: String, token: String, uri: Uri, upsert: Boolean = false) = uploadToSignedUrlAsFlow(path, token, UploadData(uri.readChannel(), uri.contentSize), upsert)

/**
 * Updates a file in [BucketApi.bucketId] under [path]
 * @param path The path to update the file to
 * @param uri The uri to update
 * @param upsert Whether to overwrite an existing file
 * @return the key to the updated file
 */
suspend fun BucketApi.update(path: String, uri: Uri, upsert: Boolean = false) = update(path, UploadData(uri.readChannel(), uri.contentSize), upsert)

/**
 * Updates a file in [BucketApi.bucketId] under [path]
 * @param path The path to update the file to
 * @param uri The uri to update
 * @param upsert Whether to overwrite an existing file
 * @return A flow that emits the upload progress and at last the key to the updated file
 */
fun BucketApi.updateAsFlow(path: String, uri: Uri, upsert: Boolean = false) = updateAsFlow(path, UploadData(uri.readChannel(), uri.contentSize), upsert)

private fun Uri.readChannel(): ByteReadChannel {
    val context = applicationContext()
    val inputStream = context.contentResolver.openInputStream(this) ?: throw IllegalArgumentException("Uri is not readable")
    return inputStream.use {
        it.toByteReadChannel()
    }
}