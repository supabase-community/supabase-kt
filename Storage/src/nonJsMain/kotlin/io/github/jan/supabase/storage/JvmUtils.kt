package io.github.jan.supabase.storage

import io.github.jan.supabase.annotiations.SupabaseExperimental
import java.io.File
import java.nio.file.Path
import kotlin.io.path.readBytes
import kotlin.io.path.writeBytes

/**
 * Uploads a file in [BucketApi.bucketId] under [path]
 * @param path The path to upload the file to
 * @param file The file to upload
 * @param upsert Whether to overwrite an existing file
 * @return the key to the uploaded file
 */
suspend fun BucketApi.upload(path: String, file: File, upsert: Boolean = false) = upload(path, file.readBytes(), upsert)

/**
 * Uploads a file in [BucketApi.bucketId] under [path]
 * @param path The path to upload the file to
 * @param file The file to upload
 * @param upsert Whether to overwrite an existing file
 * @return A flow that emits the upload progress and at last the key to the uploaded file
 */
@SupabaseExperimental
suspend fun BucketApi.uploadAsFlow(path: String, file: File, upsert: Boolean = false) = uploadAsFlow(path, file.readBytes(), upsert)

/**
 * Uploads a file in [BucketApi.bucketId] under [path]
 * @param path The path to upload the file to
 * @param file The file to upload
 * @param upsert Whether to overwrite an existing file
 * @return the key to the uploaded file
 */
suspend fun BucketApi.upload(path: String, file: Path, upsert: Boolean = false) = upload(path, file.readBytes(), upsert)

/**
 * Uploads a file in [BucketApi.bucketId] under [path]
 * @param path The path to upload the file to
 * @param file The file to upload
 * @param upsert Whether to overwrite an existing file
 * @return A flow that emits the upload progress and at last the key to the uploaded file
 */
@SupabaseExperimental
suspend fun BucketApi.uploadAsFlow(path: String, file: Path, upsert: Boolean = false) = uploadAsFlow(path, file.readBytes(), upsert)

/**
 * Uploads a file in [BucketApi.bucketId] under [path] using a presigned url
 * @param path The path to upload the file to
 * @param token The presigned url token
 * @param file The file to upload
 * @param upsert Whether to overwrite an existing file
 * @return the key to the uploaded file
 */
suspend fun BucketApi.uploadToSignedUrl(path: String, token: String, file: File, upsert: Boolean = false) = uploadToSignedUrl(path, token, file.readBytes(), upsert)

/**
 * Uploads a file in [BucketApi.bucketId] under [path] using a presigned url
 * @param path The path to upload the file to
 * @param token The presigned url token
 * @param file The file to upload
 * @param upsert Whether to overwrite an existing file
 * @return A flow that emits the upload progress and at last the key to the uploaded file
 */
@SupabaseExperimental
suspend fun BucketApi.uploadToSignedUrlAsFlow(path: String, token: String, file: File, upsert: Boolean = false) = uploadToSignedUrlAsFlow(path, token, file.readBytes(), upsert)

/**
 * Uploads a file in [BucketApi.bucketId] under [path] using a presigned url
 * @param path The path to upload the file to
 * @param token The presigned url token
 * @param file The file to upload
 * @param upsert Whether to overwrite an existing file
 */
suspend fun BucketApi.uploadToSignedUrl(path: String, token: String, file: Path, upsert: Boolean = false) = uploadToSignedUrl(path, token, file.readBytes(), upsert)

/**
 * Uploads a file in [BucketApi.bucketId] under [path] using a presigned url
 * @param path The path to upload the file to
 * @param token The presigned url token
 * @param file The file to upload
 * @param upsert Whether to overwrite an existing file
 * @return A flow that emits the upload progress and at last the key to the uploaded file
 */
@SupabaseExperimental
suspend fun BucketApi.uploadToSignedUrlAsFlow(path: String, token: String, file: Path, upsert: Boolean = false) = uploadToSignedUrlAsFlow(path, token, file.readBytes(), upsert)

/**
 * Updates a file in [BucketApi.bucketId] under [path]
 * @param path The path to be updated
 * @param file The new file
 * @param upsert Whether to overwrite an existing file
 */
suspend fun BucketApi.update(path: String, file: Path, upsert: Boolean = false) = update(path, file.readBytes(), upsert)

/**
 * Updates a file in [BucketApi.bucketId] under [path]
 * @param path The path to be updated
 * @param file The new file
 * @param upsert Whether to overwrite an existing file
 * @return A flow that emits the upload progress and at last the key to the uploaded file
 */
@SupabaseExperimental
suspend fun BucketApi.updateAsFlow(path: String, file: Path, upsert: Boolean = false) = updateAsFlow(path, file.readBytes(), upsert)

/**
 * Updates a file in [BucketApi.bucketId] under [path]
 * @param path The path to be updated
 * @param file The new file
 * @param upsert Whether to overwrite an existing file
 */
suspend fun BucketApi.update(path: String, file: File, upsert: Boolean = false) = update(path, file.readBytes(), upsert)

/**
 * Updates a file in [BucketApi.bucketId] under [path]
 * @param path The path to be updated
 * @param file The new file
 * @param upsert Whether to overwrite an existing file
 * @return A flow that emits the upload progress and at last the key to the uploaded file
 */
@SupabaseExperimental
suspend fun BucketApi.updateAsFlow(path: String, file: File, upsert: Boolean = false) = updateAsFlow(path, file.readBytes(), upsert)

/**
 * Downloads a file from [BucketApi.bucketId] under [path] and saves it to [file]
 * @param path The path to download the file from
 * @param file The file to save the data to
 */
suspend fun BucketApi.downloadAuthenticatedTo(path: String, file: File) {
    val bytes = downloadAuthenticated(path)
    file.writeBytes(bytes)
}

/**
 * Downloads a file from [BucketApi.bucketId] under [path] and saves it to [file]
 * @param path The path to download the file from
 * @param file The file to save the data to
 */
suspend fun BucketApi.downloadAuthenticatedTo(path: String, file: Path) {
    val bytes = downloadAuthenticated(path)
    file.writeBytes(bytes)
}

/**
 * Downloads a file from [BucketApi.bucketId] under [path] and saves it to [file]
 * @param path The path to download the file from
 * @param file The file to save the data to
 */
suspend fun BucketApi.downloadPublicTo(path: String, file: File) {
    val bytes = downloadPublic(path)
    file.writeBytes(bytes)
}

/**
 * Downloads a file from [BucketApi.bucketId] under [path] and saves it to [file]
 * @param path The path to download the file from
 * @param file The file to save the data to
 */
suspend fun BucketApi.downloadPublicTo(path: String, file: Path) {
    val bytes = downloadPublic(path)
    file.writeBytes(bytes)
}