package io.github.jan.supacompose.storage

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
 * @return the key to the uploaded file
 */
suspend fun BucketApi.upload(path: String, file: Path) = upload(path, file.readBytes())

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