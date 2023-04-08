package io.github.jan.supabase.storage

import java.io.File
import java.nio.file.Path
import kotlin.io.path.readBytes
import kotlin.io.path.writeBytes

/**
 * Uploads a file in [BucketApi.bucketId] under [path]
 * @param path The path to upload the file to
 * @param file The file to upload
 * @return the key to the uploaded file
 */
suspend fun BucketApi.upload(path: String, file: File) = upload(path, file.readBytes())

/**
 * Uploads a file in [BucketApi.bucketId] under [path]
 * @param path The path to upload the file to
 * @param file The file to upload
 * @return A flow that emits the upload progress and at last the key to the uploaded file
 */
suspend fun BucketApi.uploadAsFlow(path: String, file: File) = uploadAsFlow(path, file.readBytes())

/**
 * Uploads a file in [BucketApi.bucketId] under [path]
 * @param path The path to upload the file to
 * @param file The file to upload
 * @return the key to the uploaded file
 */
suspend fun BucketApi.upload(path: String, file: Path) = upload(path, file.readBytes())

/**
 * Uploads a file in [BucketApi.bucketId] under [path]
 * @param path The path to upload the file to
 * @param file The file to upload
 * @return A flow that emits the upload progress and at last the key to the uploaded file
 */
suspend fun BucketApi.uploadAsFlow(path: String, file: Path) = uploadAsFlow(path, file.readBytes())

/**
 * Uploads a file in [BucketApi.bucketId] under [path] using a presigned url
 * @param path The path to upload the file to
 * @param token The presigned url token
 * @param file The file to upload
 * @return the key to the uploaded file
 */
suspend fun BucketApi.uploadToSignedUrl(path: String, token: String, file: File) = uploadToSignedUrl(path, token, file.readBytes())

/**
 * Uploads a file in [BucketApi.bucketId] under [path] using a presigned url
 * @param path The path to upload the file to
 * @param token The presigned url token
 * @param file The file to upload
 * @return A flow that emits the upload progress and at last the key to the uploaded file
 */
suspend fun BucketApi.uploadToSignedUrlAsFlow(path: String, token: String, file: File) = uploadToSignedUrlAsFlow(path, token, file.readBytes())

/**
 * Uploads a file in [BucketApi.bucketId] under [path] using a presigned url
 * @param path The path to upload the file to
 * @param file The presigned url token
 * @param file The file to upload
 */
suspend fun BucketApi.uploadToSignedUrl(path: String, token: String, file: Path) = uploadToSignedUrl(path, token, file.readBytes())

/**
 * Uploads a file in [BucketApi.bucketId] under [path] using a presigned url
 * @param path The path to upload the file to
 * @param token The presigned url token
 * @param file The file to upload
 * @return A flow that emits the upload progress and at last the key to the uploaded file
 */
suspend fun BucketApi.uploadToSignedUrlAsFlow(path: String, token: String, file: Path) = uploadToSignedUrlAsFlow(path, token, file.readBytes())

/**
 * Updates a file in [BucketApi.bucketId] under [path]
 * @param path The path to be updated
 * @param file The new file
 */
suspend fun BucketApi.update(path: String, file: Path) = update(path, file.readBytes())

/**
 * Updates a file in [BucketApi.bucketId] under [path]
 * @param path The path to be updated
 * @param file The new file
 * @return A flow that emits the upload progress and at last the key to the uploaded file
 */
suspend fun BucketApi.updateAsFlow(path: String, file: Path) = updateAsFlow(path, file.readBytes())

/**
 * Updates a file in [BucketApi.bucketId] under [path]
 * @param path The path to be updated
 * @param file The new file
 */
suspend fun BucketApi.update(path: String, file: File) = update(path, file.readBytes())

/**
 * Updates a file in [BucketApi.bucketId] under [path]
 * @param path The path to be updated
 * @param file The new file
 * @return A flow that emits the upload progress and at last the key to the uploaded file
 */
suspend fun BucketApi.updateAsFlow(path: String, file: File) = updateAsFlow(path, file.readBytes())

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