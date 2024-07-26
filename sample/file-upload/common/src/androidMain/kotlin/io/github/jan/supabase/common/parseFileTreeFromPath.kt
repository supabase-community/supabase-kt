package io.github.jan.supabase.common

import io.github.jan.supabase.storage.resumable.ResumableClient
import io.github.jan.supabase.storage.resumable.ResumableUpload
import io.github.vinceglb.filekit.core.PlatformFile
import kotlinx.coroutines.Deferred

actual fun parseFileTreeFromPath(path: String): List<PlatformFile> {
    TODO("Not yet implemented")
}

actual fun parseFileTreeFromURIs(paths: List<String>): List<PlatformFile> {
    TODO("Not yet implemented")
}

actual suspend fun ResumableClient.continuePreviousPlatformUploads(): List<Deferred<ResumableUpload>> = throw UnsupportedOperationException()
//not supported as you loose access to the uri's file after the app is closed