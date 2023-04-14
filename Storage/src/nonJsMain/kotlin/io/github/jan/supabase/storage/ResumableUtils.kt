package io.github.jan.supabase.storage

import io.github.jan.supabase.storage.resumable.Fingerprint
import io.github.jan.supabase.storage.resumable.ResumableClient
import io.ktor.util.cio.readChannel
import java.io.File
import java.nio.file.Path
import kotlin.io.path.fileSize

/**
 * Creates a new resumable upload or continues an existing one.
 * If there is an url in the cache for the given [Fingerprint], the upload will be continued.
 * @param file The file to upload
 * @param path The path to upload the data to
 * @param upsert Whether to overwrite existing files
 */
suspend fun ResumableClient.createOrContinueUpload(file: File, path: String, upsert: Boolean = false) = createOrContinueUpload(file.readChannel(), file.length(), path, upsert)

/**
 * Creates a new resumable upload or continues an existing one.
 * If there is an url in the cache for the given [Fingerprint], the upload will be continued.
 * @param file The file to upload
 * @param path The path to upload the data to
 * @param upsert Whether to overwrite existing files
 */
suspend fun ResumableClient.createOrContinueUpload(file: Path, path: String, upsert: Boolean = false) = createOrContinueUpload(file.readChannel(), file.fileSize(), path, upsert)