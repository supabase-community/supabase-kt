package io.github.jan.supabase.common

import io.github.jan.supabase.storage.continuePreviousUriUploads
import io.github.jan.supabase.storage.resumable.ResumableClient
import io.github.jan.supabase.storage.resumable.ResumableUpload
import kotlinx.coroutines.Deferred

actual fun parseFileTreeFromPath(path: String): List<MPFile> {
    TODO("Not yet implemented")
}

actual fun parseFileTreeFromURIs(paths: List<String>): List<MPFile> {
    TODO("Not yet implemented")
}

actual suspend fun ResumableClient.continuePreviousPlatformUploads(): List<Deferred<ResumableUpload>> = throw UnsupportedOperationException()
//not supported as you loose access to the uri's file after the app is closed