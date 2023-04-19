package io.github.jan.supabase.storage

import android.net.Uri
import io.github.jan.supabase.annotiations.SupabaseExperimental

/**
 * Uploads a file in [BucketApi.bucketId] under [path]
 * @param path The path to upload the file to
 * @param uri The uri to upload
 * @param upsert Whether to overwrite an existing file
 * @return the key to the updated file
 */
suspend fun BucketApi.upload(path: String, uri: Uri, upsert: Boolean = false) = upload(path, uri.readBytes(), upsert)

/**
 * Uploads a file in [BucketApi.bucketId] under [path]
 * @param path The path to upload the file to
 * @param uri The uri to upload
 * @param upsert Whether to overwrite an existing file
 * @return A flow that emits the upload progress and at last the key to the updated file
 */
@SupabaseExperimental
suspend fun BucketApi.uploadAsFlow(path: String, uri: Uri, upsert: Boolean = false) = uploadAsFlow(path, uri.readBytes(), upsert)

/**
 * Uploads a file in [BucketApi.bucketId] under [path] using a presigned url
 * @param path The path to upload the file to
 * @param token The presigned url token
 * @param uri The uri to upload
 * @return the key to the updated file
 */
suspend fun BucketApi.uploadToSignedUrl(path: String, token: String, uri: Uri, upsert: Boolean = false) = uploadToSignedUrl(path, token, uri.readBytes(), upsert)

/**
 * Uploads a file in [BucketApi.bucketId] under [path] using a presigned url
 * @param path The path to upload the file to
 * @param token The presigned url token
 * @param uri The uri to upload
 * @param upsert Whether to overwrite an existing file
 * @return A flow that emits the upload progress and at last the key to the updated file
 */
@SupabaseExperimental
suspend fun BucketApi.uploadToSignedUrlAsFlow(path: String, token: String, uri: Uri, upsert: Boolean = false) = uploadToSignedUrlAsFlow(path, token, uri.readBytes(), upsert)

/**
 * Updates a file in [BucketApi.bucketId] under [path]
 * @param path The path to update the file to
 * @param uri The uri to update
 * @param upsert Whether to overwrite an existing file
 * @return the key to the updated file
 */
suspend fun BucketApi.update(path: String, uri: Uri, upsert: Boolean = false) = update(path, uri.readBytes(), upsert)

/**
 * Updates a file in [BucketApi.bucketId] under [path]
 * @param path The path to update the file to
 * @param uri The uri to update
 * @param upsert Whether to overwrite an existing file
 * @return A flow that emits the upload progress and at last the key to the updated file
 */
@SupabaseExperimental
suspend fun BucketApi.updateAsFlow(path: String, uri: Uri, upsert: Boolean = false) = updateAsFlow(path, uri.readBytes(), upsert)

private fun Uri.readBytes(): ByteArray {
    val context = applicationContext()
    val inputStream = context.contentResolver.openInputStream(this) ?: throw IllegalArgumentException("Uri is not readable")
    return inputStream.use {
        it.readBytes()
    }
}