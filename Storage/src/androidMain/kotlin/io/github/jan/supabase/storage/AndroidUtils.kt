package io.github.jan.supabase.storage

import android.net.Uri
import io.github.jan.supabase.annotiations.SupabaseExperimental

suspend fun BucketApi.upload(path: String, uri: Uri, upsert: Boolean = false) = upload(path, uri.readBytes(), upsert)

@SupabaseExperimental
suspend fun BucketApi.uploadAsFlow(path: String, uri: Uri, upsert: Boolean = false) = uploadAsFlow(path, uri.readBytes(), upsert)

suspend fun BucketApi.uploadToSignedUrl(path: String, token: String, uri: Uri, upsert: Boolean = false) = uploadToSignedUrl(path, token, uri.readBytes(), upsert)

@SupabaseExperimental
suspend fun BucketApi.uploadToSignedUrlAsFlow(path: String, token: String, uri: Uri, upsert: Boolean = false) = uploadToSignedUrlAsFlow(path, token, uri.readBytes(), upsert)

suspend fun BucketApi.update(path: String, uri: Uri, upsert: Boolean = false) = update(path, uri.readBytes(), upsert)

@SupabaseExperimental
suspend fun BucketApi.updateAsFlow(path: String, uri: Uri, upsert: Boolean = false) = updateAsFlow(path, uri.readBytes(), upsert)

private fun Uri.readBytes(): ByteArray {
    val context = applicationContext()
    val inputStream = context.contentResolver.openInputStream(this) ?: throw IllegalArgumentException("Uri is not readable")
    return inputStream.use {
        it.readBytes()
    }
}