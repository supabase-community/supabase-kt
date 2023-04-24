package io.github.jan.supabase.storage

import io.github.jan.supabase.annotiations.SupabaseExperimental
import io.ktor.util.cio.readChannel
import io.ktor.util.cio.writeChannel
import kotlinx.coroutines.flow.onEach
import java.io.File
import java.nio.file.Path
import kotlin.io.path.fileSize
import kotlin.io.path.writeBytes
import kotlin.io.path.writer

/**
 * Uploads a file in [BucketApi.bucketId] under [path]
 * @param path The path to upload the file to
 * @param file The file to upload
 * @param upsert Whether to overwrite an existing file
 * @return the key to the uploaded file
 */
suspend fun BucketApi.upload(path: String, file: File, upsert: Boolean = false) = upload(path, UploadData(file.readChannel(), file.length()), upsert)

/**
 * Uploads a file in [BucketApi.bucketId] under [path]
 * @param path The path to upload the file to
 * @param file The file to upload
 * @param upsert Whether to overwrite an existing file
 * @return A flow that emits the upload progress and at last the key to the uploaded file
 */
@SupabaseExperimental
suspend fun BucketApi.uploadAsFlow(path: String, file: File, upsert: Boolean = false) = uploadAsFlow(path, UploadData(file.readChannel(), file.length()), upsert)

/**
 * Uploads a file in [BucketApi.bucketId] under [path]
 * @param path The path to upload the file to
 * @param file The file to upload
 * @param upsert Whether to overwrite an existing file
 * @return the key to the uploaded file
 */
suspend fun BucketApi.upload(path: String, file: Path, upsert: Boolean = false) = upload(path, UploadData(file.readChannel(), file.fileSize()), upsert)

/**
 * Uploads a file in [BucketApi.bucketId] under [path]
 * @param path The path to upload the file to
 * @param file The file to upload
 * @param upsert Whether to overwrite an existing file
 * @return A flow that emits the upload progress and at last the key to the uploaded file
 */
@SupabaseExperimental
suspend fun BucketApi.uploadAsFlow(path: String, file: Path, upsert: Boolean = false) = uploadAsFlow(path, UploadData(file.readChannel(), file.fileSize()), upsert)

/**
 * Uploads a file in [BucketApi.bucketId] under [path] using a presigned url
 * @param path The path to upload the file to
 * @param token The presigned url token
 * @param file The file to upload
 * @param upsert Whether to overwrite an existing file
 * @return the key to the uploaded file
 */
suspend fun BucketApi.uploadToSignedUrl(path: String, token: String, file: File, upsert: Boolean = false) = uploadToSignedUrl(path, token, UploadData(file.readChannel(), file.length()), upsert)

/**
 * Uploads a file in [BucketApi.bucketId] under [path] using a presigned url
 * @param path The path to upload the file to
 * @param token The presigned url token
 * @param file The file to upload
 * @param upsert Whether to overwrite an existing file
 * @return A flow that emits the upload progress and at last the key to the uploaded file
 */
@SupabaseExperimental
suspend fun BucketApi.uploadToSignedUrlAsFlow(path: String, token: String, file: File, upsert: Boolean = false) = uploadToSignedUrlAsFlow(path, token, UploadData(file.readChannel(), file.length()), upsert)

/**
 * Uploads a file in [BucketApi.bucketId] under [path] using a presigned url
 * @param path The path to upload the file to
 * @param token The presigned url token
 * @param file The file to upload
 * @param upsert Whether to overwrite an existing file
 */
suspend fun BucketApi.uploadToSignedUrl(path: String, token: String, file: Path, upsert: Boolean = false) = uploadToSignedUrl(path, token, UploadData(file.readChannel(), file.fileSize()), upsert)

/**
 * Uploads a file in [BucketApi.bucketId] under [path] using a presigned url
 * @param path The path to upload the file to
 * @param token The presigned url token
 * @param file The file to upload
 * @param upsert Whether to overwrite an existing file
 * @return A flow that emits the upload progress and at last the key to the uploaded file
 */
@SupabaseExperimental
suspend fun BucketApi.uploadToSignedUrlAsFlow(path: String, token: String, file: Path, upsert: Boolean = false) = uploadToSignedUrlAsFlow(path, token, UploadData(file.readChannel(), file.fileSize()), upsert)

/**
 * Updates a file in [BucketApi.bucketId] under [path]
 * @param path The path to be updated
 * @param file The new file
 * @param upsert Whether to overwrite an existing file
 */
suspend fun BucketApi.update(path: String, file: Path, upsert: Boolean = false) = update(path, UploadData(file.readChannel(), file.fileSize()), upsert)

/**
 * Updates a file in [BucketApi.bucketId] under [path]
 * @param path The path to be updated
 * @param file The new file
 * @param upsert Whether to overwrite an existing file
 * @return A flow that emits the upload progress and at last the key to the uploaded file
 */
@SupabaseExperimental
suspend fun BucketApi.updateAsFlow(path: String, file: Path, upsert: Boolean = false) = updateAsFlow(path, UploadData(file.readChannel(), file.fileSize()), upsert)

/**
 * Updates a file in [BucketApi.bucketId] under [path]
 * @param path The path to be updated
 * @param file The new file
 * @param upsert Whether to overwrite an existing file
 */
suspend fun BucketApi.update(path: String, file: File, upsert: Boolean = false) = update(path, UploadData(file.readChannel(), file.length()), upsert)

/**
 * Updates a file in [BucketApi.bucketId] under [path]
 * @param path The path to be updated
 * @param file The new file
 * @param upsert Whether to overwrite an existing file
 * @return A flow that emits the upload progress and at last the key to the uploaded file
 */
@SupabaseExperimental
suspend fun BucketApi.updateAsFlow(path: String, file: File, upsert: Boolean = false) = updateAsFlow(path, UploadData(file.readChannel(), file.length()), upsert)

/**
 * Downloads a file from [BucketApi.bucketId] under [path] and saves it to [file]
 * @param path The path to download the file from
 * @param file The file to save the data to
 */
suspend fun BucketApi.downloadAuthenticatedTo(path: String, file: File, transform: ImageTransformation.() -> Unit = {}) = downloadAuthenticated(path, file.writeChannel(), transform)

@SupabaseExperimental
suspend fun BucketApi.downloadAuthenticatedToAsFlow(path: String, file: File, transform: ImageTransformation.() -> Unit = {}) = downloadAuthenticatedAsFlow(path, file.writeChannel(), transform)

/**
 * Downloads a file from [BucketApi.bucketId] under [path] and saves it to [file]
 * @param path The path to download the file from
 * @param file The file to save the data to
 */
suspend fun BucketApi.downloadAuthenticatedTo(path: String, file: Path, transform: ImageTransformation.() -> Unit = {}) {
    val bytes = downloadAuthenticated(path, transform)
    file.writeBytes(bytes)
}

@SupabaseExperimental
suspend fun BucketApi.downloadAuthenticatedToAsFlow(path: String, file: Path, transform: ImageTransformation.() -> Unit = {}) = downloadAuthenticatedAsFlow(path, transform).onEach {
    if(it is DownloadStatus.Success) file.writeBytes(it.data)
}

/**
 * Downloads a file from [BucketApi.bucketId] under [path] and saves it to [file]
 * @param path The path to download the file from
 * @param file The file to save the data to
 */
suspend fun BucketApi.downloadPublicTo(path: String, file: File, transform: ImageTransformation.() -> Unit = {}) = downloadPublic(path, file.writeChannel(), transform)

@SupabaseExperimental
suspend fun BucketApi.downloadPublicToAsFlow(path: String, file: File, transform: ImageTransformation.() -> Unit = {}) = downloadPublicAsFlow(path, file.writeChannel(), transform)

/**
 * Downloads a file from [BucketApi.bucketId] under [path] and saves it to [file]
 * @param path The path to download the file from
 * @param file The file to save the data to
 */
suspend fun BucketApi.downloadPublicTo(path: String, file: Path, transform: ImageTransformation.() -> Unit = {}) {
    val bytes = downloadPublic(path, transform)
    file.writeBytes(bytes)
}

@SupabaseExperimental
suspend fun BucketApi.downloadPublicToAsFlow(path: String, file: Path, transform: ImageTransformation.() -> Unit = {}) = downloadPublicAsFlow(path, transform)